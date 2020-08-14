resource "aws_instance" "ingest" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "c5.4xlarge"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.ingest.name

    vpc_security_group_ids = [aws_security_group.ingest.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${var.prefix}ingest"
        AnsibleGroups = "ingest"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "ingest" {
    name = "${var.prefix}ingest"
    role = aws_iam_role.ingest.name
}

resource "aws_iam_role" "ingest" {

    name = "${var.prefix}ingest"

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

    name   = "${var.prefix}ingest"
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
