provider "aws" {
    version = "~> 2.61"
    region  = "eu-central-1"
}



# --------------------------------------------
#      EC2 instances
# --------------------------------------------

resource "aws_instance" "flink" {

    ami                    = "ami-0be110ffd53859e30"
    instance_type          = "t3.micro"
    vpc_security_group_ids = [aws_security_group.basic_security.id]
    key_name               = aws_key_pair.deploy.key_name

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}flink"
        Project = "eda"
        AnsibleGroup = "flink"
    }
}



# --------------------------------------------
#      Security
# --------------------------------------------

resource "aws_security_group" "basic_security" {

    name = "${local.name_prefix}basic_security"

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
        Project = "eda"
    }
}

resource "aws_key_pair" "deploy" {

    key_name   = "${local.name_prefix}deploy"
    public_key = file("${var.ssh_key}.pub")

    tags = {
        Project = "eda"
    }
}



# --------------------------------------------
#      Output Values
# --------------------------------------------

output "flink_host" {
    value = aws_instance.flink.public_dns
}
