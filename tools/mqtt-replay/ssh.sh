#!/bin/bash

root=`realpath "$(dirname "$0")"`
ssh_command=`cd $root && $root/../terraform-inventory/terraform_inventory.py --ssh`
eval $ssh_command
