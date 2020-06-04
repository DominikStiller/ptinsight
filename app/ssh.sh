#!/bin/bash

root=$(realpath "$(dirname "$0")")
ssh_command=$(cd $root && $root/../tools/terraform-inventory/terraform_inventory.py --ssh $@)
eval $ssh_command
