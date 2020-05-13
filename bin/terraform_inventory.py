#!/usr/bin/env python3
# -*- coding: utf-8 -*-
'''
This script creates an Ansible inventory from a Terraform tfstate file
'''

# see https://jacobsalmela.com/2019/07/27/how-to-create-a-custom-dynamic-inventory-script-for-ansible/

import json
import sys

tfstate_file = "terraform/terraform.tfstate"

# load the tfstat file
with open(tfstate_file, 'r') as fh:
    tfstate = json.load(fh)

instances = (instance for instance in tfstate['resources'] if instance['type'] == 'aws_instance')


def ansible_list():
    inventory = {
        "_meta": {
            "hostvars": {}
        }
    }

    def add_host(group, public_ip_addr, vars):
        if group not in inventory:
            inventory[group] = {"hosts": []}

        inventory[group]['hosts'].append(public_ip_addr)
        inventory['_meta']['hostvars'][public_ip_addr] = vars

    for instance in instances:
        for sub_instance in instance['instances']:
            sub_instance_attributes = sub_instance['attributes']
            tags = sub_instance_attributes['tags']

            # Only add public EC2 instances
            if 'public_ip' in sub_instance_attributes:
                public_ip_addr = sub_instance_attributes['public_ip']
                vars = {
                    tag[11:]: tags[tag] for tag in tags if tag.startswith("AnsibleVar_")
                }

                add_host('all', public_ip_addr, vars)

                if 'AnsibleGroup' in tags:
                    group = tags['AnsibleGroup']
                    add_host(group, public_ip_addr, vars)

    print(json.dumps(inventory, indent=2))


def ssh_host():
    for instance in instances:
        for sub_instance in instance['instances']:
            sub_instance_attributes = sub_instance['attributes']
            if 'public_ip' in sub_instance_attributes:
                print(sub_instance_attributes['public_ip'])
                return


if sys.argv[1] == '--list':
    ansible_list()
elif sys.argv[1] == '--ssh-host':
    ssh_host()

# Implementing --host is not necessary because _meta is populated in --list
# https://docs.ansible.com/ansible/latest/dev_guide/developing_inventory.html#tuning-the-external-inventory-script
