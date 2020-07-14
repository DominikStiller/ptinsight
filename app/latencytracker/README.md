# PT Insight: Latency Tracker

The latency tracker component is responsible for tracking the end-to-end latency, i.e. the delay between the
 ingestion of an event and the time at which results based on this event become visible. The latency tracker also
  describes partial latencies as described in the [design document](../docs/Latency%20Tracking.md).


## Prerequisites
* Python 3.8
* poetry


## Usage

Install dependencies (`poetry install`) and enter the venv (`poetry shell`) before starting. Then execute with
 `python -m ptinsight.latencytracker`.


## Configuration

Default: `config/latencytracker.default.yaml`  
Local: `config/latencytracker.yaml`  (create a copy of default)  
Deployed: `../ansible/roles/latencytracker-deploy/templates/latencytracker.yaml.j2`
