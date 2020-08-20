resource "aws_instance" "visualization" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.nano"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.visualization.name

    vpc_security_group_ids = [aws_security_group.visualization.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${var.prefix}visualization"
        AnsibleGroups = "visualization"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "visualization" {
    name = "${var.prefix}visualization"
    role = aws_iam_role.visualization.name
}

resource "aws_iam_role" "visualization" {

    name = "${var.prefix}visualization"

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

resource "aws_security_group" "visualization" {

    name   = "${var.prefix}visualization"
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
        description = "Visualization frontend from trusted CIDRs"
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
