import logging
from typing import Dict

from expiringdict import ExpiringDict
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
from ptinsight.common import Event
from ptinsight.common.events import unpack_event_details
from ptinsight.common.hslrealtime import HSLRealtimeLatencyMarkers
from ptinsight.common.serialize import deserialize

from ptinsight.latencytracker.statistics import LatencyStatistics

logger = logging.getLogger(__name__)


class LatencyTracker:
    def __init__(self, config: dict):
        if "protobuf_format" in config["kafka"]:
            self.protobuf_format = config["kafka"].pop("protobuf_format")
        else:
            self.protobuf_format = "json"
        # Needs to have the same config as the Flink consumer
        self.consumer = KafkaConsumer(**config["kafka"])

        origin = (-48.875, -123.393)
        self.h3_resolution = int(config["latency_markers"]["h3_resolution"])
        h3_max_k = int(config["latency_markers"]["h3_max_k"])
        self._latency_markers = HSLRealtimeLatencyMarkers(
            origin, self.h3_resolution, h3_max_k
        )

        self.seen_markers: Dict[int, LatencyStatistics] = ExpiringDict(
            max_len=10000, max_age_seconds=30
        )

    def start(self) -> None:
        try:
            self.consumer.subscribe(pattern="ingress.*|egress.*")
            for message in self.consumer:
                topic = message.topic
                event = deserialize(message.value, self.protobuf_format)

                if details := unpack_event_details(topic, event):
                    if geocell := self._latency_markers.check_latency_marker(details):
                        if topic.startswith("ingress."):
                            self._process_ingress(geocell, event)
                        elif topic.startswith("egress."):
                            self._process_egress(topic, geocell, event)

        except NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")

    def _process_ingress(self, geocell: int, event: Event):
        statistics = LatencyStatistics()
        statistics.mark_ingress(event)
        self.seen_markers[geocell] = statistics

    def _process_egress(self, topic: str, geocell: int, event: Event):
        if geocell in self.seen_markers:
            statistics = self.seen_markers[geocell]
            statistics.mark_egress(event)
            statistics.job = topic[7:]
            statistics.print()

            # Remove markers that were observed end-to-end so that they are only counted once
            # Otherwise they would appear with increasing latency when being contained in multiple sliding windows
            del self.seen_markers[geocell]
