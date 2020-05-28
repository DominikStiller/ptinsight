.ONESHELL:
.SILENT:

ROOT := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

default:
	@echo "no default target"

all: apply deploy

# Terraform
define terraform_cmd =
	cd ${ROOT}terraform
	TF_STATE=. terraform $(1) -auto-approve --var deployment=${USER}
endef
init:
	cd ${ROOT}terraform
	terraform init
apply:
	$(call terraform_cmd, "apply")
destroy:
	$(call terraform_cmd, "destroy")
reapply: destroy apply

# Ansible
define ansible_cmd =
	ANSIBLE_HOST_KEY_CHECKING=False\
	ansible-playbook -i ${ROOT}../terraform-inventory/terraform_inventory.py $(1) record --timeout 60 ${ROOT}ansible/site.yml
endef
deploy:
	$(call ansible_cmd, "--skip-tags")
record:
	$(call ansible_cmd, "--tags")
