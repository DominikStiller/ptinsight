# Apache Flink (non-clustered)

## Use Case
* Input: MQTT feed from HSL.fi (Helsinki Transport Agency)
* Output: Number of bus/tram arrivals/departures in a 5 s window

## Deployment

1. Initialize Terraform
```
make init
```

_Note: Steps 2-4 can be executed in a single command using `make all`_

2. Set up infrastructure using Terraform, you might need to wait a couple of seconds after this for the server to boot
```
make apply
```

3. Install Flink and Kafka using Ansible, pulling the inventory from the Terraform state.
```
make setup
```

4. Deploy Flink job using Ansible, pulling the inventory from the Terraform state.
```
make deploy
```

5. Get the host name from 2. or the IP from 3. and open the Flink Web UI under `http://host:8081/`. You can view the job output in the task manager's stdout tab.

6. View the Kafka topic.
```
./ssh.sh
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic events
```

## Makefile Targets

* `all`: apply, setup, deploy
* `apply`: Set up AWS infrastructure
* `destroy`: Destroy AWS infrastructure
* `reapply`: Destroy, then set up AWS infrastructure
* `setup`: Install Flink and Kafka
* `deploy`: Deploy Flink job
