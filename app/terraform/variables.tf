variable "ssh_key" {
    description = "SSH key for EC2 instances"
    default = "~/.ssh/id_rsa_eda_deployer"
}

variable "trusted_cidr" {
    description = "CIDRs which are allowed to access security groups"
    default = ["46.223.0.0/16"]
}

variable "deployment" {
    description = "Prefix for AWS resources to separate deployments"
    default = ""
}

locals {
    name_prefix = "${var.deployment != "" ? "${var.deployment}-" : ""}ptinsight-"
}
