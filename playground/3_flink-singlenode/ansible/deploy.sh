#!/bin/bash

ansible_root=`realpath "$(dirname "$0")"`
terraform_root=`realpath $ansible_root/../terraform`

# Prints warnings due to EC2 instance names and thus groups containing hyphens
# TF_STATE is workaround for issue https://github.com/adammck/terraform-inventory/issues/144
TF_STATE=$terraform_root ANSIBLE_HOST_KEY_CHECKING=False\
    ansible-playbook -v -i ~/.local/bin/terraform-inventory\
    -u centos --private-key ~/.ssh/id_rsa_eda_deployer\
    $ansible_root/site.yml
