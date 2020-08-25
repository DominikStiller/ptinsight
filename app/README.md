# PT Insight - Real-Time Analytics for Public Transportation Data

This is a demo application for stream analytics. It will be referred to as "PT Insight" (Public Transportation Insight).
For general documentation, see `docs`. For component-specific documentation, see their folders.


## Use Case
* Ingest real-time event stream from [Helsinki Public Transportation API](https://digitransit.fi/en/developers/apis/4-realtime-api/)
* Analyze for following:
    * Vehicle distribution across city
    * Delay distribution across city
    * Final stop distribution across city
    * Flow direction patterns
    * Emergency stops/hard braking
* Display that information in a heatmap as overlay for a city map


## Requirements
* Correctness: results should be guaranteed to be correct through exactly-once event-time-order processing even if events arrive out of order and faults occur
* Fault tolerance: the solution should guarantee consistent results and preserve state during faults without heavy recomputations
* Low latency: results based on an ingested event should become available for visualization in (near) real time (a latency of a few seconds is acceptable/inevitable, depending on the job)
* Scalability: the solution should be able handle large volumes of data without performance degradation


## Architecture

![Architecture](docs/images/architecture.png)


## Directory Structure
* `analysis`: Jupyter notebooks for latency and data analysis
* `ansible`: Ansible playbook and roles to setup and deploy the PT Insight system
* `common`: Common libraries shared between components, including protobuf definitions
* `docs`: Detailed docs for system-wide components
* `ingestion`: Component for ingesting external events into the PT Insight system
* `latencytracker`: Component for tracking end-to-end latency of the PT Insight system
* `processing`: Component for performing streaming analytics (Flink jobs)
* `terraform`: Terraform configuration files for cloud infrastructure setup
* `visualization`: Component for visualizing events


## Development

### Environment Setup
Set up your environment as described in the [top-level README](../README.md), and set up the individual components as desribed in their READMEs.
Then add your IP address block to the `trusted_cidr` variable in `terraform/variables.tf` to grant access to EC2 instances from your computer.

### Deployment

1. Initialize Terraform
```
make init
```

_Note: Steps 2-4 can be executed in a single command using `make all`, which is equivalent to `make apply setup deploy`_

2. Set up infrastructure using Terraform. Rerun this step when you change the AWS setup in the .tf files.
```
make apply

# Alternatively when you want to rebuild an existing infrastructure
make reapply  # equivalent to "make destroy apply"
```

3. Install platforms (Java, Python, Flink, Kafka...). Rerun this step when you change server configurations.
```
make setup

# Alternatively for individual components:
make setup-zookeeper
make setup-kafka
make setup-processing
make setup-ingestion
make setup-visualization
make setup-latencytracker
```

4. Deploy applications. Rerun this step when you change application code.
```
make deploy

# Alternatively for individual components:
make deploy-kafka
make deploy-processing
make deploy-ingestion
make deploy-visualization
make deploy-latencytracker
```

5. Open the visualization in your browser
```
make show-hosts  # get visualization host
Navigate to http://visualization-host:8080/
```

6. SSH into the servers
```
./ssh.sh kafka [0-2]
./ssh.sh flink_master
./ssh.sh flink_worker [0-3]
./ssh.sh ingestion
./ssh.sh visualization
./ssh.sh latencytracker
```

7. Destroy infrastructure when it is not needed anymore
```
make destroy
```

## Links
* List of Helsinki APIs: https://www.notion.so/faa753c34e1f469d92750c13f7f9d0d8?v=ba0f9f25b9a34d31afba6d05db2ffa96
