from datetime import datetime
import logging
import multiprocessing
from multiprocessing import Process
from typing import Dict

from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
from ptinsight.common import Event
from ptinsight.common.events import unpack_event_details
from ptinsight.common.hslrealtime import HSLRealtimeLatencyMarkers
from ptinsight.common.latency import LatencyMarker
from ptinsight.common.serialize import deserialize

from ptinsight.latencytracker.recorder import Recorder

logger = logging.getLogger(__name__)


class LatencyTracker:
    def __init__(self, config: dict):
        if "protobuf_format" in config["kafka"]:
            self.protobuf_format = config["kafka"].pop("protobuf_format")
        else:
            self.protobuf_format = "json"
        self.num_partitions_per_topic = int(
            config["kafka"].pop("num_partitions_per_topic")
        )

        # Needs to have the same config as the Flink consumer
        self.kafka_config = config["kafka"]

        self.h3_resolution = int(config["latency_markers"]["h3_resolution"])
        h3_max_k = int(config["latency_markers"]["h3_max_k"])
        self._latency_markers = HSLRealtimeLatencyMarkers(self.h3_resolution, h3_max_k)

        self.seen_markers: Dict[int, LatencyMarker] = None
        self.measurement_id = (
            datetime.utcnow().replace(microsecond=0).isoformat().replace(":", "-")
        )

    def start(self) -> None:
        with multiprocessing.Manager() as manager:
            seen_markers = manager.dict()

            pool = []
            for i in range(self.num_partitions_per_topic):
                p = Process(
                    target=self._start, args=(self.measurement_id, i, seen_markers)
                )
                pool.append(p)
                p.start()

            multiprocessing.connection.wait(p.sentinel for p in pool)

    def _start(self, measurement_id, index, seen_markers) -> None:
        self.seen_markers = seen_markers
        self.recorder = Recorder(measurement_id, index)

        try:
            # Use unique consumer group since we want to read from the last offset
            # Skipping to the last offset seems to not work on Linux
            consumer = KafkaConsumer(
                **self.kafka_config,
                group_id=f"ptinsight-latencytracker-{measurement_id}",
            )
            consumer.subscribe(pattern="input.*|analytics.*")

            for message in consumer:
                topic = message.topic
                event = deserialize(message.value, self.protobuf_format)

                if details := unpack_event_details(topic, event):
                    if geocell := self._latency_markers.check_latency_marker(details):
                        if topic.startswith("input."):
                            self._process_ingress(geocell, event)
                        elif topic.startswith("analytics."):
                            self._process_egress(topic, geocell, event)

        except NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")

    def _process_ingress(self, geocell: int, event: Event):
        marker = LatencyMarker()
        marker.mark_ingress(event)
        self.seen_markers[geocell] = marker

    def _process_egress(self, topic: str, geocell: int, event: Event):
        if geocell in self.seen_markers:
            job = topic[10:]
            marker = self.seen_markers[geocell]

            # Ensure that each marker is observed only once for each job
            # Otherwise they would appear with increasing latency when being contained in multiple sliding windows
            if not job in marker.seen_jobs:
                logger.debug(f"Recording egress marker for {job}")
                marker.mark_egress(event, job)
                self.recorder.write(marker)
