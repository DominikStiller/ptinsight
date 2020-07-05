import logging

import h3
from flask_socketio import SocketIO
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
from ptinsight.common import (
    Event,
    FlowDirection,
    FinalStopCount,
    DelayStatistics,
    VehicleCount,
    EmergencyStop,
)
from ptinsight.common.serialize import deserialize

logger = logging.getLogger(__name__)


class KafkaToSocketioBridge:
    """A relay for Kafka protobuf messages to socket.io JSON messages"""

    def __init__(self, socketio: SocketIO, kafkaConfig: dict):
        if "protobuf_format" in kafkaConfig:
            self.protobuf_format = kafkaConfig.pop("protobuf_format")
        else:
            self.protobuf_format = "json"

        self.socketio = socketio
        self.consumer = KafkaConsumer(**kafkaConfig)

    def start(self):
        try:
            self.consumer.subscribe(
                [
                    "egress.vehicle-count",
                    "egress.delay-statistics",
                    "egress.flow-direction",
                    "egress.final-stop-count",
                    "egress.emergency-stop",
                ]
            )
            for message in self.consumer:
                topic = message.topic
                event = deserialize(message.value, self.protobuf_format)
                self._emit(topic, event)

        except NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")

    def _emit(self, topic, event: Event):
        if topic == "egress.vehicle-count":
            vehicle_count = VehicleCount()
            event.details.Unpack(vehicle_count)

            self.socketio.emit(
                "vehicle-count",
                {
                    "geocell": h3.h3_to_string(vehicle_count.geocell),
                    "count": vehicle_count.count,
                },
            )
        elif topic == "egress.delay-statistics":
            delay_statistics = DelayStatistics()
            event.details.Unpack(delay_statistics)

            self.socketio.emit(
                "delay-statistics",
                {
                    "geocell": h3.h3_to_string(delay_statistics.geocell),
                    "p50": delay_statistics.percentile50th,
                    "p90": delay_statistics.percentile90th,
                    "p99": delay_statistics.percentile99th,
                },
            )
        elif topic == "egress.flow-direction":
            flow_direction = FlowDirection()
            event.details.Unpack(flow_direction)

            # if flow_direction.count < 3:
            #     return

            self.socketio.emit(
                "flow-direction",
                {
                    "edge": h3.h3_to_string(flow_direction.geocells_edge),
                    "count": flow_direction.count,
                },
            )
        elif topic == "egress.final-stop-count":
            final_stop_count = FinalStopCount()
            event.details.Unpack(final_stop_count)

            self.socketio.emit(
                "final-stop-count",
                {
                    "geocell": h3.h3_to_string(final_stop_count.geocell),
                    "count": final_stop_count.count,
                },
            )
        elif topic == "egress.emergency-stop":
            emergency_stop = EmergencyStop()
            event.details.Unpack(emergency_stop)

            self.socketio.emit(
                "emergency-stop",
                {
                    "lat": emergency_stop.latitude,
                    "lon": emergency_stop.longitude,
                    "max_deceleration": emergency_stop.max_deceleration,
                    "speed_diff": emergency_stop.speed_diff,
                },
            )
