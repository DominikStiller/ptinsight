variable "ssh_privatekey" {
    description = "SSH private key file for EC2 instances"
    default = "~/.ssh/id_rsa_ptinsight_deploy"
}

variable "trusted_cidr" {
    description = "CIDRs which are allowed to access security groups"
    default = ["46.223.0.0/16"]
}

variable "user" {
    description = "Deployment user to use as prefix for AWS resources to separate deployments"
    default = ""
}

locals {
    prefix = "${var.user != "" ? "${var.user}-" : ""}ptinsight-"
}
