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
    instance_type          = "t3.nano"
    vpc_security_group_ids = [aws_security_group.basic_security.id]
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.mqttrecorder.name

    root_block_device {
        delete_on_termination = true
        volume_size = 50
    }

    tags = {
        Name = "${local.name_prefix}mqttrecorder"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "mqttrecorder" {
  name = "${local.name_prefix}mqttrecorder"
  role = aws_iam_role.mqttrecorder.name
}

resource "aws_iam_role" "mqttrecorder" {

  name = "${local.name_prefix}mqttrecorder"

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

resource "aws_iam_role_policy" "s3_write" {

    role = aws_iam_role.mqttrecorder.name

    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "List",
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket",
                "s3:ListObjectsV2"
            ],
            "Resource": "arn:aws:s3:::mqtt-recordings"
        },
        {
            "Sid": "Write",
            "Effect": "Allow",
            "Action": [
                "s3:HeadObject",
                "s3:GetObject",
                "s3:PutObject"
            ],
            "Resource": "arn:aws:s3:::mqtt-recordings/*"
        }
    ]
}
EOF
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

    egress {
        from_port   = 0
        to_port     = 0
        protocol    = -1
        cidr_blocks = ["0.0.0.0/0"]
    }
}

resource "aws_key_pair" "deploy" {

    key_name   = "${local.name_prefix}deploy"
    public_key = file("${var.ssh_key}.pub")
}



# --------------------------------------------
#      Output Values
# --------------------------------------------

output "host" {
    value = aws_instance.instance.public_dns
}
