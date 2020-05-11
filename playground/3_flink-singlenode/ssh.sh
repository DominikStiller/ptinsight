#!/bin/bash

root=`realpath "$(dirname "$0")"`
host=`cd $root && $root/../../bin/terraform_inventory.py --ssh-host`
ssh -i ~/.ssh/id_rsa_eda_deployer centos@$host
