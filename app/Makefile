.ONESHELL:
.SILENT:

ROOT := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

default:
	@echo "no default target"

all: apply setup deploy

# Terraform
define terraform_cmd =
	cd ${ROOT}terraform
	terraform $(1) -auto-approve --var deployment=${USER}
endef
init:
	cd ${ROOT}terraform
	terraform init
apply:
	$(call terraform_cmd, "apply")
destroy:
	$(call terraform_cmd, "destroy")
reapply: destroy apply
show-hosts:
	cd ${ROOT}terraform
	terraform output

# Ansible
define ansible_cmd =
	ANSIBLE_CONFIG=${ROOT}ansible/ansible.cfg \
	ansible-playbook -i ${ROOT}../tools/terraform-inventory/terraform_inventory.py --tags $(1) ${ROOT}ansible/site.yml
endef
setup:
	$(call ansible_cmd, "setup")
setup-processing:
	$(call ansible_cmd, "setup-processing")
setup-kafka:
	$(call ansible_cmd, "setup-kafka")
setup-ingest:
	$(call ansible_cmd, "setup-ingest")
setup-ui:
	$(call ansible_cmd, "setup-ui")
deploy:
	$(call ansible_cmd, "deploy")
deploy-processing:
	$(call ansible_cmd, "deploy-processing")
deploy-kafka:
	$(call ansible_cmd, "deploy-kafka")
deploy-ingest:
	$(call ansible_cmd, "deploy-ingest")
deploy-ui:
	$(call ansible_cmd, "deploy-ui")
