# --------------------------------------------
#      Input Variables
# --------------------------------------------

variable "ssh_key" {
    description = "SSH key for EC2 instances"
}

variable "trusted_cidr" {
    description = "CIDRs which are allowed to access security groups"
}


provider "aws" {
    region = "eu-central-1"
}



# --------------------------------------------
#      EC2 instances
# --------------------------------------------

resource "aws_instance" "instance" {
    ami                    = "ami-0be110ffd53859e30"
    instance_type          = "t3.micro"
    vpc_security_group_ids = [aws_security_group.security_group.id]
    key_name               = aws_key_pair.key.key_name

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "eda_flink"
        project = "eda"
    }
}



# --------------------------------------------
#      Security
# --------------------------------------------

resource "aws_security_group" "security_group" {

    name = "eda_dev"

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

    tags = {
        project = "eda"
    }
}

resource "aws_key_pair" "key" {

    key_name   = "eda_deploy"
    public_key = file("${var.ssh_key}.pub")

    tags = {
        project = "eda"
    }
}

# --------------------------------------------
#      Output Values
# --------------------------------------------


output "flink_host" {
    value = aws_instance.instance.public_dns
}
