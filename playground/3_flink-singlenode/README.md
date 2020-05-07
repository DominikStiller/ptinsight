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
terraform init
```

2. Set up infrastructure specified in `main.tf` using Terraform
```
terraform apply
```

3. Deploy Flink + Application using Ansible, pulling the inventory from the Terraform state.
```
./deploy_flink.sh
```

4. Open the host name from 2. or the IP from 3. in your browser.
