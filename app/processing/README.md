# PT Insight: Processing

The ingest component is responsible for detecting complex events from a stream of primitive events.
It receives the stream from Kafka and sends complex events back to Kafka.



## Prerequisites
* Java 11


## Usage

Run via `./gradlew run` or create distribution jar via `./gradlew clean shadowJar`. It might not run locally if the Kafka cluster is inaccessible.
