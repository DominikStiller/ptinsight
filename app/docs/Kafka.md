# Kafka

Apache Kafka is used as event transport and storage backbone. Every topic only contains a single protobuf message type.


## Topics
Every topic only contains protobuf messages of a single type. Topics are prefixed with `ingress` if they are produced in `ingest`, and `egress` if they are produced in `processing`.

All topics are replicated on all 3 hosts and have two partitions. Records are randomly assigned to a partition, i.e. no keys are used and the order of records is only preserved within a partition.

| Topic                   | Description                            | Source                | Protobuf Type                                   |
| ----------------------- | -------------------------------------- | --------------------- | ----------------------------------------------- |
| ingress.arrival         | Arrival of vehicle at stop             | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.Arrival         |
| ingress.departure       | Departure of vehicle from stop         | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.Departure       |
| ingess.vehicle-position | Position and speed of vehicle          | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.VehiclePosition |
| egress.vehicle-count    | Number of vehicles per geocell         | PT Insight Processing | com.dxc.ptinsight.proto.egress.VehicleCount     |
| egress.delay-statistics | Arrival delay statistics per cell      | PT Insight Processing | com.dxc.ptinsight.proto.egress.DelayStatistics  |
| egress.flow-direction   | Flow direction between cells           | PT Insight Processing | com.dxc.ptinsight.proto.egress.FlowDirection    |
| egress.final-stop-count | Number of vehicles per final stop cell | PT Insight Processing | com.dxc.ptinsight.proto.egress.FinalStopCount   |
| egress.emergency-stop   | High deceleration of vehicle           | PT Insight Processing | com.dxc.ptinsight.proto.egress.EmergencyStop    |


## Addresses
Kafka brokers listen to connections from inside the VPC on port 9092, but to external connections on port 9093.


## Debugging
Print a topic to console live:
```
# On Kafka host
./ssh.sh kafka [0|1|2]
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server [private-ip]:9092 --topic [topic]

# From local computer (assuming Kafka is installed)
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server [public-ip]:9093 --topic [topic]
```

List topics:
```
# On Kafka host
./ssh.sh kafka [0|1|2]
/opt/kafka/bin/kafka-topics.sh --bootstrap-server [private-ip]:9092 --describe

# From local computer (assuming Kafka is installed)
/opt/kafka/bin/kafka-topics.sh --bootstrap-server [public-ip]:9093 --describe
```
