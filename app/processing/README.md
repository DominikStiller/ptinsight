# PT Insight: Processing

The processing component is responsible for detecting complex events from a stream of primitive events.
It receives the stream from Kafka and sends complex events back to Kafka.



## Prerequisites
* Java 11


## Usage

Run via `./gradlew run` or create distribution jar via `./gradlew clean shadowJar`. It might not run locally if the Kafka cluster is inaccessible.


## Configuration

Default: `config/processing.default.yaml`  
Local: `config/processing.yaml`  (create a copy of default)
Deployed: `../ansible/roles/processing-deploy/templates/processing.yaml.j2`
