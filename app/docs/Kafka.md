# Kafka

Apache Kafka is used as event transport and storage backbone. Every topic only contains a single protobuf message type.


## Topics
Every topic only contains protobuf messages of a single type. Topics are prefixed with `input` if they are produced in `ingestion`, and `analytics` if they are produced in `processing`.

All topics are replicated on all 3 hosts and have two partitions. Records are randomly assigned to a partition, i.e. no keys are used and the order of records is only preserved within a partition.

| Topic                                        | Description                                      | Source                | Protobuf Type                                                  |
| -------------------------------------------- | ------------------------------------------------ | --------------------- | -------------------------------------------------------------- |
| input.arrival                                | Arrival of vehicle at stop                       | HSL.fi MQTT           | com.dxc.ptinsight.proto.input.Arrival                          |
| input.departure                              | Departure of vehicle from stop                   | HSL.fi MQTT           | com.dxc.ptinsight.proto.input.Departure                        |
| ingess.vehicle-position                      | Position and speed of vehicle                    | HSL.fi MQTT           | com.dxc.ptinsight.proto.input.VehiclePosition                  |
| analytics.vehicle-distribution               | Number of vehicles per geocell                   | PT Insight Processing | com.dxc.ptinsight.proto.analytics.VehicleDistributionResult    |
| analytics.delay-distribution                 | Arrival delay statistics per cell                | PT Insight Processing | com.dxc.ptinsight.proto.analytics.DelayDistributionResult      |
| analytics.flow-direction                     | Flow direction between cells                     | PT Insight Processing | com.dxc.ptinsight.proto.analytics.FlowDirectionResult          |
| analytics.final-stop-distribution            | Number of vehicles per final stop cell           | PT Insight Processing | com.dxc.ptinsight.proto.analytics.FinalStopDistributionResult  |
| analytics.emergency-stop-detection-table     | High deceleration of vehicle using table API     | PT Insight Processing | com.dxc.ptinsight.proto.analytics.EmergencyStopDetectionResult |
| analytics.emergency-stop-detection-streaming | High deceleration of vehicle using streaming API | PT Insight Processing | com.dxc.ptinsight.proto.analytics.EmergencyStopDetectionResult |


### Adding a new topic
Execute these steps to add a new topic:
1. Add the topic to the table above
2. Add the topic to the Kafka deployment role in `ansible/role/kafka-deploy/tasks/main.yml`
3. Add the topic to the Python mapping in `common/python/ptinsight/common/events.py`
4. Run `make deploy-kafka`


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
