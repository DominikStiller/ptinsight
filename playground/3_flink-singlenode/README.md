# Apache Flink (non-clustered)

## Use Case
* Input: MQTT feed from HSL.fi (Helsinki Transport Agency)
* Outputs:
    * Time a bus needed between two stops (if possible, relative to estimated time)
    * Number of bus stops in a 10 s window
    * Trigger some other action for true EDA

## Deployment

1. Initialize Terraform
```
cd terraform
terraform init
```

2. Set up infrastructure using Terraform
```
terraform/apply.sh
```

3. Deploy Flink + Application using Ansible, pulling the inventory from the Terraform state.
```
ansible/deploy.sh
```

4. Get the host name from 2. or the IP from 3. and open the Flink Web UI under `http://host:8081/`.

## Scripts

### Terraform
* `terraform/apply.sh`: Set up infrastructure
* `terraform/destroy.sh`: Destroy infrastructure
* `terraform/reapply.sh`: Destroy, then set up infrastructure

### Ansible
* `ansible/deploy.sh [tags]`: Run Ansible playbook
    * `no tags`: Set up Flink and deploy application
    * `setup,deploy`: Set up Flink and deploy application
    * `setup`: Set up Flink
    * `deploy`: Deploy application

