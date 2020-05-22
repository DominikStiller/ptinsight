#!/bin/bash

root=`realpath "$(dirname "$0")"`
login=`cd $root && $root/../../bin/terraform_inventory.py --ssh-login`
ssh -i ~/.ssh/id_rsa_eda_deployer $login
