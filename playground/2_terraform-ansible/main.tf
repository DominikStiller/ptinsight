variable "ssh_key" {
    default = "~/.ssh/id_rsa_eda_deploy"
}

provider "aws" {
    region = "eu-central-1"
}

resource "aws_instance" "instance" {
    ami = "ami-0be110ffd53859e30"
    instance_type = "t3.micro"
    vpc_security_group_ids = [aws_security_group.security_group.id]
    key_name = aws_key_pair.key.key_name

    tags = {
        Name = "eda-tftest"
        project = "eda"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_key
    }
}

resource "aws_security_group" "security_group" {
    name = "eda-dev"

    # SSH
    ingress {
        from_port   = 22
        to_port     = 22
        protocol    = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    # HTTP
    ingress {
        from_port   = 80
        to_port     = 80
        protocol    = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }

    tags = {
        project = "eda"
    }
}

resource "aws_key_pair" "key" {
    key_name = "eda-deployer"
    public_key = file("${var.ssh_key}.pub")

    tags = {
        project = "eda"
    }
}

output "host" {
    value = aws_instance.instance.public_dns
}
