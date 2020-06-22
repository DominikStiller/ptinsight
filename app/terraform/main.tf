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

# ---- >> Core (Processing + Kafka) ---------------
resource "aws_instance" "core" {
    count = 3

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.medium"
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.core.name

    vpc_security_group_ids = [aws_security_group.core.id]
    subnet_id              = aws_subnet.main.id

    root_block_device {
        delete_on_termination = true
        volume_size = 16
    }

    tags = {
        Name = "${local.name_prefix}core-${count.index}"
        AnsibleGroups = "processing,kafka,zookeeper"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "core" {
    name = "${local.name_prefix}core"
    role = aws_iam_role.core.name
}

resource "aws_iam_role" "core" {

    name = "${local.name_prefix}core"

    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
            "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow",
            "Sid": ""
        }
    ]
}
EOF
}

resource "aws_security_group" "core" {

    name   = "${local.name_prefix}core"
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
# ---- << Core ------------------------------

# ---- >> Ingest ----------------------------
resource "aws_instance" "ingest" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.nano"
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
            "Effect": "Allow",
            "Sid": ""
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
            "Effect": "Allow",
            "Sid": ""
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



# --------------------------------------------
#      Output Values
# --------------------------------------------

output "core_host" {
    value = aws_instance.core.*.public_ip
}
output "ingest_host" {
    value = aws_instance.ingest.public_ip
}
output "ui_host" {
    value = aws_instance.ui.public_ip
}
