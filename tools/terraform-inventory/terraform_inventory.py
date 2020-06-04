#!/usr/bin/env python3
"""A dynamic inventory script for Ansible for Terraform tfstate files"""

import json
import sys
import os


def ansible_list(instances, args):
    inventory = {"_meta": {"hostvars": {}}}

    def add_host(group, ip_addr):
        if group not in inventory:
            inventory[group] = {"hosts": []}
        inventory[group]["hosts"].append(ip_addr)

    for instance in instances:
        for sub_instance in instance["instances"]:
            sub_instance_attributes = sub_instance["attributes"]
            tags = sub_instance_attributes["tags"]

            # Only add public EC2 instances
            if "public_ip" in sub_instance_attributes:
                public_ip_addr = sub_instance_attributes["public_ip"]

                # Extract Ansible variables from tags
                inventory["_meta"]["hostvars"][public_ip_addr] = {
                    tag[11:]: tags[tag] for tag in tags if tag.startswith("AnsibleVar_")
                }

                # Add to all group and groups specified in tags
                add_host("all", public_ip_addr)
                if "AnsibleGroups" in tags:
                    for group in tags["AnsibleGroups"].split(","):
                        add_host(group, public_ip_addr)

    print(json.dumps(inventory, indent=2))


def list_private(instances, args):
    inventory = {}

    def add_host(group, ip_addr):
        if group not in inventory:
            inventory[group] = []
        inventory[group].append(ip_addr)

    for instance in instances:
        for sub_instance in instance["instances"]:
            sub_instance_attributes = sub_instance["attributes"]
            tags = sub_instance_attributes["tags"]

            # Only add public EC2 instances
            if "private_ip" in sub_instance_attributes:
                private_ip_addr = sub_instance_attributes["private_ip"]

                # Add to all group and groups specified in tags
                if "AnsibleGroups" in tags:
                    for group in tags["AnsibleGroups"].split(","):
                        add_host(group, private_ip_addr)

    print(json.dumps(inventory, indent=2))


def ssh(instances, args):
    commands = []

    for instance in instances:
        for sub_instance in instance["instances"]:
            sub_instance_attributes = sub_instance["attributes"]
            tags = sub_instance_attributes["tags"]

            # Only use public EC2 instances
            if "public_ip" in sub_instance_attributes:
                command = "ssh "

                # Private key
                if (
                    "AnsibleVar_ansible_ssh_private_key"
                    in sub_instance_attributes["tags"]
                ):
                    key = sub_instance_attributes["tags"][
                        "AnsibleVar_ansible_ssh_private_key"
                    ]
                    command += f"-i {key} "

                # Host and user
                host = sub_instance_attributes["public_ip"]
                if "AnsibleVar_ansible_user" in sub_instance_attributes["tags"]:
                    user = sub_instance_attributes["tags"]["AnsibleVar_ansible_user"]
                    command += f"{user}@{host}"
                else:
                    command += host

                # Ansible groups
                if "AnsibleGroups" in tags:
                    groups = tags["AnsibleGroups"].split(",")
                else:
                    groups = []

                commands.append((command, groups))

    if not args:
        print(commands[0][0])
    else:
        for command, groups in commands:
            if args[0] in groups:
                print(command)
                return
        print(f"No host for group {args[0]} found")


def main():
    # Implementing --host is not necessary because _meta is populated in --list
    # https://docs.ansible.com/ansible/latest/dev_guide/developing_inventory.html#tuning-the-external-inventory-script
    if sys.argv[1] == "--list":
        command = ansible_list
    elif sys.argv[1] == "--list-private":
        command = list_private
    elif sys.argv[1] == "--ssh":
        command = ssh
    else:
        print("Unrecognized command")
        return

    # Find .tfstate file
    path = None
    for p in ["terraform.tfstate", "terraform/terraform.tfstate"]:
        if os.path.isfile(p):
            path = p
    if not path:
        print("No .tfstate file found")
        return

    # Load EC2 instances
    with open(path, "r") as file:
        tfstate = json.load(file)
    instances = (
        instance
        for instance in tfstate["resources"]
        if instance["type"] == "aws_instance"
    )

    command(instances, sys.argv[2:])


if __name__ == "__main__":
    main()
