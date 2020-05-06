#!/bin/bash

# Prints warnings due to EC2 instance names and thus groups containing hyphens
# TF_STATE is workaround for issue https://github.com/adammck/terraform-inventory/issues/144
TF_STATE=. ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i ~/.local/bin/terraform-inventory -u centos --private-key ~/.ssh/id_rsa_eda_deployer playbook.yaml
