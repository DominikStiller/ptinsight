resource "aws_instance" "ui" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.nano"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.ui.name

    vpc_security_group_ids = [aws_security_group.ui.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${var.prefix}ui"
        AnsibleGroups = "ui"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "ui" {
    name = "${var.prefix}ui"
    role = aws_iam_role.ui.name
}

resource "aws_iam_role" "ui" {

    name = "${var.prefix}ui"

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

    name   = "${var.prefix}ui"
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
