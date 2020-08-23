import logging

import h3
from flask_socketio import SocketIO
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
from ptinsight.common import Event, VehicleType
from ptinsight.common.events import unpack_event_details
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
                    "analytics.vehicle-distribution",
                    "analytics.delay-distribution",
                    "analytics.flow-direction",
                    "analytics.final-stop-distribution",
                    "analytics.emergency-stop-detection-streaming",
                ]
            )
            for message in self.consumer:
                topic = message.topic
                event = deserialize(message.value, self.protobuf_format)
                self._emit(topic, event)

        except NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")

    def _emit(self, topic: str, event: Event):
        details = unpack_event_details(topic, event)

        if topic == "analytics.vehicle-distribution":
            data = {
                "geocell": h3.h3_to_string(details.geocell),
                "count": details.count,
            }
        elif topic == "analytics.delay-distribution":
            data = {
                "geocell": h3.h3_to_string(details.geocell),
                "p50": details.percentile50th,
                "p90": details.percentile90th,
                "p99": details.percentile99th,
            }
        elif topic == "analytics.flow-direction":
            data = {
                "edge": h3.h3_to_string(details.geocells_edge),
                "count": details.count,
            }
        elif topic == "analytics.final-stop-distribution":
            data = {
                "geocell": h3.h3_to_string(details.geocell),
                "count": details.count,
            }
        elif topic == "analytics.emergency-stop-detection-streaming":
            data = {
                "veh_type": VehicleType.Name(details.vehicle_type).lower(),
                "lat": details.latitude,
                "lon": details.longitude,
                "max_dec": details.max_deceleration,
                "speed_diff": details.speed_diff,
            }
        else:
            return

        self.socketio.emit(
            topic, {"timestamp": event.event_timestamp.ToMilliseconds(), "data": data}
        )
