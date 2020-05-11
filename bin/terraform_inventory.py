#!/usr/bin/env python3
# -*- coding: utf-8 -*-
'''
This script creates an Ansible inventory from a Terraform tfstate file
'''

# see https://jacobsalmela.com/2019/07/27/how-to-create-a-custom-dynamic-inventory-script-for-ansible/

import json
import sys

tfstate_file = "terraform/terraform.tfstate"
debug = False

# load the tfstat file
with open(tfstate_file, 'r') as fh:
    tfstate = json.load(fh)

instances = (instance for instance in tfstate['resources'] if instance['type'] == 'aws_instance')


def ansible_list():
    inventory = {
        "all": {
            "hosts": []
        },
        "_meta": {
            "hostvars": {}
        }
    }

    for instance in instances:
        for num, sub_instance in enumerate(instance['instances']):
            sub_instance_attributes = sub_instance['attributes']
            try:
                public_ip_addr = sub_instance_attributes['public_ip']

                inventory['all']['hosts'].append(public_ip_addr)

                try:
                    group = sub_instance_attributes['tags']['AnsibleGroup']

                    if group not in inventory:
                        inventory[group] = {"hosts": []}

                    inventory[group]['hosts'].append(public_ip_addr)

                except KeyError:
                    pass

            except KeyError:
                pass

    if debug:
        print(json.dumps(inventory, indent=2), file=sys.stderr)
    print(json.dumps(inventory, indent=2))


def ssh_host():
    for instance in instances:
        for num, sub_instance in enumerate(instance['instances']):
            sub_instance_attributes = sub_instance['attributes']
            try:
                public_ip_addr = sub_instance_attributes['public_ip']
                print(public_ip_addr)
                return

            except KeyError:
                pass


def ansible_host(hostname):
    host = {
        "network": {
            "ip": ""
        }
    }

    for instance in instances:
        if not instance['name'] == hostname:
            continue
        for num, sub_instance in enumerate(instance['instances']):
            sub_instance_attributes = sub_instance['attributes']
            if sub_instance_attributes['public_ip']:
                host['network']['ip'] = sub_instance_attributes['public_ip']

    if debug:
        print(json.dumps(host, indent=2), file=sys.stderr)
    print(json.dumps(host, indent=2))


if debug:
    print(sys.argv, file=sys.stderr)

if sys.argv[1] == '--list':
    ansible_list()
elif sys.argv[1] == '--ssh-host':
    ssh_host()
elif sys.argv[1] == '--host':
    ansible_host(sys.argv[2])

sys.exit(0)
