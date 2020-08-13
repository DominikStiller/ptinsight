provider "aws" {
    version = "~> 2.61"
    region  = "eu-central-1"
}



# --------------------------------------------
#      EC2 instances
# --------------------------------------------

# ---- >> Flink ------------------------------
resource "aws_instance" "flink" {

    ami                    = "ami-04cf43aca3e6f3de3"
    instance_type          = "t3.medium"
    vpc_security_group_ids = [aws_security_group.basic_security.id]
    key_name               = aws_key_pair.deploy.key_name
    iam_instance_profile   = aws_iam_instance_profile.flink.name

    root_block_device {
        delete_on_termination = true
    }

    tags = {
        Name = "${local.name_prefix}flink"
        Project = "eda"
        AnsibleGroups = "flink"
        AnsibleVar_ansible_user = "centos"
        AnsibleVar_ansible_ssh_private_key_file = var.ssh_key
    }
}

resource "aws_iam_instance_profile" "flink" {
  name = "${local.name_prefix}flink"
  role = aws_iam_role.flink.name
}

resource "aws_iam_role" "flink" {

  name = "${local.name_prefix}flink"

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

resource "aws_iam_role_policy" "flink_lambda_invoke" {
    
    role = aws_iam_role.flink.name

    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Invoke",
            "Effect": "Allow",
            "Action": [
                "lambda:InvokeFunction"
            ],
            "Resource": "${aws_lambda_function.event_handler.arn}"
        }
    ]
}
EOF
}
# ---- << Flink ------------------------------



# --------------------------------------------
#      Lambda
# --------------------------------------------

# ---- >> Lambda -----------------------------
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

    function_name = "${local.name_prefix}handle_event"
    role          = aws_iam_role.lambda.arn
    handler       = "lambda_function.handle_event"
    runtime       = "python3.8"

    filename      = local.lambda_deploy_zip
    source_code_hash = filebase64sha256(local.lambda_deploy_src)
    # With zip, updates are only triggered on the next apply
    # source_code_hash = fileexists(local.lambda_deploy_zip) ? filebase64sha256(local.lambda_deploy_zip) : ""

    depends_on = [null_resource.build_lambda]
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
    role       = aws_iam_role.lambda.name
    policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}
# ---- << Lambda -----------------------------



# --------------------------------------------
#      Security
# --------------------------------------------

resource "aws_security_group" "basic_security" {

    name = "${local.name_prefix}playground-3"

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

    key_name   = "${local.name_prefix}deploy-playground-3"
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
