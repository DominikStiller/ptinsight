resource "aws_instance" "kafka" {
    count = 3

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.large"
    key_name               = var.keypair.key_name
    iam_instance_profile   = aws_iam_instance_profile.kafka.name

    vpc_security_group_ids = [aws_security_group.kafka.id]
    subnet_id              = var.subnet.id

    root_block_device {
        delete_on_termination = true
        volume_size = 16
    }

    tags = {
        Name = "${var.prefix}kafka-${count.index}"
        AnsibleGroups = "kafka,zookeeper"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_privatekey
    }
}

resource "aws_iam_instance_profile" "kafka" {
    name = "${var.prefix}kafka"
    role = aws_iam_role.kafka.name
}

resource "aws_iam_role" "kafka" {

    name = "${var.prefix}kafka"

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

    name   = "${var.prefix}kafka"
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
