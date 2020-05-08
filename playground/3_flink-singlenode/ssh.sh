#!/bin/bash

host=`TF_STATE=terraform terraform-inventory --inventory | sed -n 2p`
ssh -i ~/.ssh/id_rsa_eda_deployer centos@$host
