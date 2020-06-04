# PT Insight: Ingest

The ingest component is responsible for ingesting events from external APIs into the appropriate Kafka topics.


## Prerequisites
* Python 3.8
* pipenv (`pip install pipenv`)


## Usage

Install dependencies (`pipenv install`) and enter the pipenv (`pipenv shell`) before starting. Then execute with `python -m ptinsight.ingest`.


## Configuration

Local: `./config/ingest.yaml`  
Deployed: `..ansible/roles/ingest-deploy/ingest.yaml.j2`
