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

2. Set up infrastructure using Terraform, you might need to wait a couple of seconds after this for the server to boot
```
make apply
```

3. Install Flink using Ansible, pulling the inventory from the Terraform state.
```
make setup
```

4. Deploy Flink job using Ansible, pulling the inventory from the Terraform state.
```
make deploy
```

5. Get the host name from 2. or the IP from 3. and open the Flink Web UI under `http://host:8081/`.

## Makefile Targets

* `all`: apply, setup, deploy
* `apply`: Set up AWS infrastructure
* `destroy`: Destroy AWS infrastructure
* `reapply`: Destroy, then set up AWS infrastructure
* `setup`: Install Flink
* `deploy`: Deploy Flink job
