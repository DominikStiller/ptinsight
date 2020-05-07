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

The deployment of the application might show reflection warnings, which are harmless (https://ci.apache.org/projects/flink/flink-docs-stable/release-notes/flink-1.10.html#java-11-support-flink-10725).

4. Get the host name from 2. or the IP from 3. and open the Flink Web UI under `http://host:8081/`.
