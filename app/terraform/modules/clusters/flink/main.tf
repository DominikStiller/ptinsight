# ---- >> Master ----------------------------
resource "aws_instance" "flink_master" {
    count = 1

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.small"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.flink_master.name

    vpc_security_group_ids = [aws_security_group.flink_master.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${var.prefix}flink-master-${count.index}"
        AnsibleGroups = "flink_master"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "flink_master" {
    name = "${var.prefix}flink_master"
    role = aws_iam_role.flink_master.name
}

resource "aws_iam_role" "flink_master" {

    name = "${var.prefix}flink_master"

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
            "Resource": "${var.flink_bucket.arn}"
        },
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${var.flink_bucket.arn}/*"
        }
    ]
}
EOF
}

resource "aws_security_group" "flink_master" {

    name   = "${var.prefix}flink_master"
    vpc_id = var.vpc.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [var.vpc.cidr_block]
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
# ---- << Master ----------------------------

# ---- >> Worker ----------------------------
resource "aws_instance" "flink_worker" {
    count = 6

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.medium"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.flink_worker.name

    vpc_security_group_ids = [aws_security_group.flink_worker.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
        volume_size = 8
    }

    tags = {
        Name = "${var.prefix}flink-worker-${count.index}"
        AnsibleGroups = "flink_worker"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "flink_worker" {
    name = "${var.prefix}flink_worker"
    role = aws_iam_role.flink_worker.name
}

resource "aws_iam_role" "flink_worker" {

    name = "${var.prefix}flink_worker"

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
            "Resource": "${var.flink_bucket.arn}"
        },
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "${var.flink_bucket.arn}/*"
        }
    ]
}
EOF
}

resource "aws_security_group" "flink_worker" {

    name   = "${var.prefix}flink_worker"
    vpc_id = var.vpc.id

    ingress {
        description = "All from VPC"
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = [var.vpc.cidr_block]
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
# ---- << Worker ----------------------------
