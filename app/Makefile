.ONESHELL:
.SILENT:

ROOT := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

default:
	@echo "no default target"

all: apply setup deploy

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
	ansible-playbook -i ${ROOT}../tools/terraform-inventory/terraform_inventory.py --tags $(1) --limit $(2) --timeout 60 ${ROOT}ansible/site.yml
endef
setup:
	$(call ansible_cmd, "setup", "all")
deploy:
	$(call ansible_cmd, "deploy", "all")
deploy-flink:
	$(call ansible_cmd, "deploy", "flink")
deploy-kafka:
	$(call ansible_cmd, "deploy", "kafka")
deploy-ingest:
	$(call ansible_cmd, "deploy", "ingest")
deploy-ui:
	$(call ansible_cmd, "deploy", "ui")
