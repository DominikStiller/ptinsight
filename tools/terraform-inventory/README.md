# Ansible Dynamic Inventory for Terraform

This tool can dynamically generate an Ansible inventory and an SSH command from the Terraform state. It implements the [required interface](https://docs.ansible.com/ansible/latest/dev_guide/developing_inventory.html#developing-inventory-scripts) for Ansible inventories.

The `terraform.tfstate` file is expected to be located in the working directory or its subfolder `terraform/`.

Instance tags:
* `AnsibleGroups`: a comma-separated list of Ansible groups to which the host belongs
* `AnsibleVar_ansible_user`: the user Ansible should use to connect to the server
* `AnsibleVar_ansible_ssh_private_key`: the private key Ansible should use to connect to the server (if not the default key, e.g. `~/.ssh/id_rsa_other`)


## Prerequisites
* Python 3


## Usage

### Ansible Dynamic Inventory
Usage (standalone): `path/to/terraform_inventory.py --list`  
Usage (with Ansible): `ansible-playbook -i path/to/terraform_inventory.py playbook.yml`

Prints the inventory as JSON.

## SSH Command
Usage (print): `path/to/terraform_inventory.py --ssh [group] [index]`  
Usage (execute): `$(path/to/terraform_inventory.py --ssh [group] [index])`

Print or execute the SSH command to connect to the first instance specified in Terraform file, automatically setting private key, user and host.
If `[group]` is specified, search instances in that Ansible group, otherwise use the first instance in the file.
If `[index]` is specified, use the index-th instance in the group, otherwise the first in the group.
