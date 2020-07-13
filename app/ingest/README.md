# PT Insight: Ingest

The ingest component is responsible for ingesting events from external APIs into the appropriate Kafka topics.
It receives messages from sources specified in the configuration and transforms them into the strongly typed protobuf
 event messages.
Invalid and unwanted events are filtered out so all Kafka topics contain valid messages.





## Prerequisites
* Python 3.8
* poetry


## Usage

Install dependencies (`poetry install`) and enter the venv (`poetry shell`) before starting. Then execute with `python -m ptinsight.ingest`.


## Configuration

Default: `config/ingest.default.yaml`  
Local: `config/ingest.yaml`  (create a copy of default)  
Deployed: `../ansible/roles/ingest-deploy/templates/ingest.yaml.j2`


## Supported Sources
* `mqtt`: A real MTT broker
* `mqtt-recording`: A recording of MQTT messages from an S3 bucket created using [mqtt-replay](../../tools/mqtt-replay)
