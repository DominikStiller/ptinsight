.ONESHELL:
.SILENT:

ROOT := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

default:
	@echo "no default target"

all: apply setup deploy

# Terraform
init:
	cd ${ROOT}/terraform
	terraform init
apply:
	cd ${ROOT}/terraform
	TF_STATE=. terraform apply -auto-approve
destroy:
	cd ${ROOT}/terraform
	TF_STATE=. terraform destroy -auto-approve
reapply:
	cd ${ROOT}/terraform
	TF_STATE=. terraform destroy -auto-approve && terraform apply -auto-approve

# Ansible
define ansible =
	TF_STATE=${ROOT}/terraform ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i ${ROOT}/../../bin/terraform_inventory.py\
		-u centos --tags $(1) --private-key ~/.ssh/id_rsa_eda_deployer --timeout 60 ${ROOT}/ansible/site.yml
endef
setup:
	$(call ansible, "setup")
deploy:
	$(call ansible, "deploy")
