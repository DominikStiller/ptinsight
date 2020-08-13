provider "aws" {
    version = "~> 2.61"
    region  = "eu-central-1"
}



# --------------------------------------------
#      EC2 instances
# --------------------------------------------

# ---- >> Instance ------------------------------
resource "aws_instance" "instance" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.small"
    vpc_security_group_ids = [aws_security_group.basic_security.id]
    key_name               = aws_key_pair.deploy.key_name

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}instance"
        Project = "eda"
        AnsibleGroups = "mosquitto,flink,simulation"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_key
    }
}
# ---- << Instance ------------------------------



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

output "host" {
    value = aws_instance.instance.public_dns
}
