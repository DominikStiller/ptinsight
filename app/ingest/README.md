# PT Insight: Ingest

The ingest component is responsible for ingesting events from external APIs into the appropriate Kafka topics.
It receives MQTT messages and polls REST APIs regularly as specified in the configuration and transforms them into the strongly typed protobuf event messages.
Invalid and unwanted events are filtered out, and, so all Kafka topics contain valid messages.



## Prerequisites
* Python 3.8
* pipenv (`pip install pipenv`)


## Usage

Install dependencies (`pipenv install --dev`) and enter the pipenv (`pipenv shell`) before starting. Then execute with `python -m ptinsight.ingest`.


## Configuration

Default: `config/ingest.default.yaml`  
Local: `config/ingest.yaml`  (create a copy of default)  
Deployed: `../ansible/roles/ingest-deploy/templates/ingest.yaml.j2`
