provider "aws" {
    version = "~> 2.61"
    region  = "eu-central-1"
}



# --------------------------------------------
#      EC2 instances
# --------------------------------------------

resource "aws_instance" "flink" {

    ami                    = "ami-0be110ffd53859e30"
    instance_type          = "t3.small"
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
#      Lambda
# --------------------------------------------

locals {
    lambda_deploy_src = "${path.module}/../lambda/lambda_function.py"
    lambda_deploy_zip = "${path.module}/../lambda/deploy.zip"
}

resource "null_resource" "build_lambda" {

    triggers = {
        hash = filebase64sha256(local.lambda_deploy_src)
    }

    provisioner "local-exec" {
        command = "${path.module}/../lambda/pack.sh"
    }
}

resource "aws_lambda_function" "event_handler" {
    filename = local.lambda_deploy_zip
    function_name = "${local.name_prefix}handle_event"
    role = aws_iam_role.lambda.arn
    handler = "lambda_function.handle_event"

    # With zip, updates are only triggered on the next apply
    # source_code_hash = fileexists(local.lambda_deploy_zip) ? filebase64sha256(local.lambda_deploy_zip) : ""
    source_code_hash = filebase64sha256(local.lambda_deploy_src)

    runtime = "python3.8"

    depends_on = [
        null_resource.build_lambda
    ]
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

resource "aws_iam_role" "lambda" {

    name = "${local.name_prefix}lambda"

    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logging_attachment" {
    role = aws_iam_role.lambda.name
    policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}



# --------------------------------------------
#      Output Values
# --------------------------------------------

output "flink_host" {
    value = aws_instance.flink.public_dns
}
