# Kafka

Apache Kafka is used as event transport and storage backbone. Every topic only contains a single protobuf message type.


## Topics

| Topic                       | Description                            | Source                | Protobuf Type                                     |
| --------------------------- | -------------------------------------- | --------------------- | ------------------------------------------------- |
| ingress.arrival             | Arrival of vehicle at stop             | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.Arrival           |
| ingress.departure           | Departure of vehicle from stop         | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.Departure         |
| ingess.vehicle-position     | Position and speed of vehicle          | HSL.fi MQTT           | com.dxc.ptinsight.proto.ingress.VehiclePosition   |
| egress.arrival-count        | Number of arrivals by vehicle type     | PT Insight Processing | com.dxc.ptinsight.proto.egress.ArrivalCount       |
| egress.vehicle-count        | Number of vehicles per geocell         | PT Insight Processing | com.dxc.ptinsight.proto.egress.VehicleCount       |
| egress.delay-statistics     | Arrival delay statistics per cell      | PT Insight Processing | com.dxc.ptinsight.proto.egress.DelayStatistics    |
| egress.flow-direction       | Flow direction between cells           | PT Insight Processing | com.dxc.ptinsight.proto.egress.FlowDirection      |
| egress.final-stop-count     | Number of vehicles per final stop cell | PT Insight Processing | com.dxc.ptinsight.proto.egress.FinalStopCount     |
| egress.emergency-stop-count | Number of emergency stops per cell     | PT Insight Processing | com.dxc.ptinsight.proto.egress.EmergencyStopCount |


## Addresses
Kafka brokers listen to connections from inside the VPC on port 9092, but to external connections on port 9093.
