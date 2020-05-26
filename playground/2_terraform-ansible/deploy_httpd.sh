#!/bin/bash

# Prints warnings due to EC2 instance names and thus groups containing hyphens
ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i ${ROOT}../../tools/terraform-inventory/terraform_inventory.py playbook.yaml
