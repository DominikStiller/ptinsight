# Kafka

Apache Kafka is used as event transport and storage backbone. Every topic only contains a single protobuf message type.


## Topics

| Topic                   | Description                   | Source      |
| ----------------------- | ----------------------------- | ----------- |
| ingress.arrival         | Arrivals of vehicles at stops | HSL.fi MQTT |
| ingess.vehicle-position | Positions of vehicles         | HSL.fi MQTT |
