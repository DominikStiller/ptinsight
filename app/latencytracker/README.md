# PT Insight: Latency Tracker

The latency tracker component is responsible for tracking the end-to-end latency, i.e. the delay between the ingestion of an event and the time at which results based on this event become visible. The latency tracker also describes partial latencies as described in the [design document](../docs/Latency%20Tracking.md). Measured latencies are stored in the `recordings/` folder.

The latency tracker is hard to parallelize. While we tried using multiprocessing, the performance was by far not sufficient. Seen markers need to be shared between processes, and Python's  multiprocessing.Manager.dict() is orders of magnitude slower than regular dicts, distorting measurements. Therefore, we only use two threads (one for consuming from Kafka, one for processing markers). However, this does not seem to have any performance benefit over single-threaded operation.


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


## Analysis
Recorded latency measurements can be analyzed in the Jupyter notebook `analysis.ipynb`. Start the Jupyter server using `poetry run jupyter-lab` and open the notebook. Plots will be stored in the `plots/` folder.
