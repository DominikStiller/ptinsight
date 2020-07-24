provider "aws" {
    version = "~> 2.64"
    region  = "eu-central-1"
}



# --------------------------------------------
#      Network
# --------------------------------------------

resource "aws_vpc" "main" {
    cidr_block           = "10.0.0.0/16"
    enable_dns_hostnames = true

    tags = {
        Name = "${local.name_prefix}main"
    }
}

resource "aws_subnet" "main" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.0.0/24"
    map_public_ip_on_launch = true

    tags = {
        Name = "${local.name_prefix}main"
    }
}

resource "aws_internet_gateway" "main" {
    vpc_id = aws_vpc.main.id

    tags = {
        Name = "${local.name_prefix}main"
    }
}

resource "aws_default_route_table" "main" {
    default_route_table_id = aws_vpc.main.default_route_table_id

    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_internet_gateway.main.id
    }

    tags = {
        Name = "${local.name_prefix}main"
    }
}



# --------------------------------------------
#      Instances
# --------------------------------------------

resource "aws_key_pair" "deploy" {

    key_name   = "${local.name_prefix}deploy"
    public_key = file("${var.ssh_key}.pub")
}

# ---- >> Kafka ------------------------------
resource "aws_instance" "kafka" {
    count = 3

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.small"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.kafka.name

    vpc_security_group_ids = [aws_security_group.kafka.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
        volume_size = 16
    }

    tags = {
        Name = "${local.name_prefix}kafka-${count.index}"
        AnsibleGroups = "kafka,zookeeper"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "kafka" {
    name = "${local.name_prefix}kafka"
    role = aws_iam_role.kafka.name
}

resource "aws_iam_role" "kafka" {

    name = "${local.name_prefix}kafka"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_security_group" "kafka" {

    name   = "${local.name_prefix}kafka"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    ingress {
        description = "Kafka from trusted CIDRs"
        from_port   = 9093
        to_port     = 9093
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}
# ---- << Kafka -----------------------------

# ---- >> Flink Master ----------------------
resource "aws_instance" "flink_master" {
    count = 1

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.small"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.flink_master.name

    vpc_security_group_ids = [aws_security_group.flink_master.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}flink-master-${count.index}"
        AnsibleGroups = "flink_master"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "flink_master" {
    name = "${local.name_prefix}flink_master"
    role = aws_iam_role.flink_master.name
}

resource "aws_iam_role" "flink_master" {

    name = "${local.name_prefix}flink_master"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "flink_master_s3" {

    role = aws_iam_role.flink_master.name

    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${aws_s3_bucket.flink.arn}"
        },
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${aws_s3_bucket.flink.arn}/*"
        }
    ]
}
EOF
}

resource "aws_security_group" "flink_master" {

    name   = "${local.name_prefix}flink_master"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    ingress {
        description = "Flink Web UI from trusted CIDRs"
        from_port   = 8081
        to_port     = 8081
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}
# ---- << Flink Master ----------------------

# ---- >> Flink Worker ----------------------
resource "aws_instance" "flink_worker" {
    count = 4

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "c5.2xlarge"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.flink_worker.name

    cpu_core_count = 4
    cpu_threads_per_core = 1

    vpc_security_group_ids = [aws_security_group.flink_worker.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
        volume_size = 8
    }

    tags = {
        Name = "${local.name_prefix}flink-worker-${count.index}"
        AnsibleGroups = "flink_worker"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "flink_worker" {
    name = "${local.name_prefix}flink_worker"
    role = aws_iam_role.flink_worker.name
}

resource "aws_iam_role" "flink_worker" {

    name = "${local.name_prefix}flink_worker"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "flink_worker_s3" {

    role = aws_iam_role.flink_worker.name

    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${aws_s3_bucket.flink.arn}"
        },
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${aws_s3_bucket.flink.arn}/*"
        }
    ]
}
EOF
}

resource "aws_security_group" "flink_worker" {

    name   = "${local.name_prefix}flink_worker"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}
# ---- << Flink Worker ----------------------

# ---- >> Ingest ----------------------------
resource "aws_instance" "ingest" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "c5.large"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.ingest.name

    vpc_security_group_ids = [aws_security_group.ingest.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}ingest"
        AnsibleGroups = "ingest"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "ingest" {
    name = "${local.name_prefix}ingest"
    role = aws_iam_role.ingest.name
}

resource "aws_iam_role" "ingest" {

    name = "${local.name_prefix}ingest"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "ingest_s3_read_recordings" {

    role = aws_iam_role.ingest.name

    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket",
                "s3:ListObjectsV2"
            ],
            "Resource": "arn:aws:s3:::mqtt-recordings"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:HeadObject",
                "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::mqtt-recordings/*"
        }
    ]
}
EOF
}

resource "aws_security_group" "ingest" {

    name   = "${local.name_prefix}ingest"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}


# ---- << Ingest ----------------------------

# ---- >> UI --------------------------------
resource "aws_instance" "ui" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.nano"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.ui.name

    vpc_security_group_ids = [aws_security_group.ui.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}ui"
        AnsibleGroups = "ui"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "ui" {
    name = "${local.name_prefix}ui"
    role = aws_iam_role.ui.name
}

resource "aws_iam_role" "ui" {

    name = "${local.name_prefix}ui"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_security_group" "ui" {

    name   = "${local.name_prefix}ui"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    ingress {
        description = "UI frontend from trusted CIDRs"
        from_port   = 8080
        to_port     = 8080
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}
# ---- << UI ---------------------------------

# ---- >> Latency Tracker --------------------
resource "aws_instance" "latencytracker" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.small"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.latencytracker.name

    vpc_security_group_ids = [aws_security_group.latencytracker.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}latencytracker"
        AnsibleGroups = "latencytracker"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "latencytracker" {
    name = "${local.name_prefix}latencytracker"
    role = aws_iam_role.latencytracker.name
}

resource "aws_iam_role" "latencytracker" {

    name = "${local.name_prefix}latencytracker"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_security_group" "latencytracker" {

    name   = "${local.name_prefix}latencytracker"
    vpc_id = aws_vpc.main.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [aws_vpc.main.cidr_block]
    }

    ingress {
        description = "SSH from trusted CIDRs"
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = var.trusted_cidr
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}
# ---- << UI ---------------------------------



# --------------------------------------------
#      Storage
# --------------------------------------------
resource "aws_s3_bucket" "flink" {
  bucket = "${local.name_prefix}flink"
  acl    = "private"
  force_destroy = true
}



# --------------------------------------------
#      Output Values
# --------------------------------------------

output "kafka_host" {
    value = aws_instance.kafka.*.public_ip
}
output "flink_master_host" {
    value = aws_instance.flink_master.*.public_ip
}
output "flink_worker_host" {
    value = aws_instance.flink_worker.*.public_ip
}
output "ingest_host" {
    value = aws_instance.ingest.public_ip
}
output "ui_host" {
    value = aws_instance.ui.public_ip
}
output "latencytracker_host" {
    value = aws_instance.latencytracker.public_ip
}
