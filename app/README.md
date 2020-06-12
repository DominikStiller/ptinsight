# PT Insight - Real-Time Analysis of Public Transportation Data

This is a demo application for large-scale complex event processing. It will be referred to as "PT Insight" (Public Transportation Insight).
For general documentations, see `docs/`. For component-specific documentation, see their folders.

## Use Case
* Ingest real-time event stream from [Helsinki Public Transportation API](https://digitransit.fi/en/developers/apis/4-realtime-api/)
* Ingest other [data like schedules, routes, service information](https://digitransit.fi/en/developers/apis/1-routing-api/), ...
* Analyze for following:
    * Detect incidents/deviations from schedule and correlate with service information
    * Detect busiest places with respect to number of arrivals
    * Detect routes with highest speeds and accelerations
* Display that information in a heatmap as overlay for a city map


## Requirements
* Resiliency to failures:
    * Requires state checkpoints and re-deployment of app to new server (Docker can aid fast deployment)


## Architecture

![Architecture](docs/images/architecture.png)


## Deployment

1. Initialize Terraform
```
make init
```

_Note: Steps 2-4 can be executed in a single command using `make all`_

1. Set up infrastructure using Terraform
```
make apply
```

1. Install platforms (Java, Python, Flink, Kafka...)
```
make setup
```

1. Deploy applications
```
make deploy
```


## Makefile Targets

* `all`: apply, setup, deploy
* `apply`: Set up AWS infrastructure
* `destroy`: Destroy AWS infrastructure
* `reapply`: Destroy, then set up AWS infrastructure
* `setup`: Install platforms
* `deploy`: Deploy everything
* `deploy-kafka`: Deploy Kafka
* `deploy-processing`: Deploy processing
* `deploy-ingest`: Deploy ingest
* `deploy-ui`: Deploy UI

## Links
* List of Helsinki APIs: https://www.notion.so/faa753c34e1f469d92750c13f7f9d0d8?v=ba0f9f25b9a34d31afba6d05db2ffa96
