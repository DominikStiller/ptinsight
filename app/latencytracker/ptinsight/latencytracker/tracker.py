import logging
from datetime import datetime
from queue import Queue
from threading import Thread
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
        # Needs to have the same config as the Flink consumer
        self.consumer = KafkaConsumer(**config["kafka"])

        self.h3_resolution = int(config["latency_markers"]["h3_resolution"])
        h3_max_k = int(config["latency_markers"]["h3_max_k"])
        self._latency_markers = HSLRealtimeLatencyMarkers(self.h3_resolution, h3_max_k)

        self.seen_markers: Dict[int, LatencyMarker] = {}

        self.recorder = Recorder()

    def start(self) -> None:
        queue = Queue()
        Thread(target=self._start_consumer, args=(queue,)).start()
        Thread(target=self._start_processing, args=(queue,)).start()

    def _start_consumer(self, queue: Queue):
        try:
            self.consumer.subscribe(pattern="input.*|analytics.*")
            for message in self.consumer:
                timestamp = int(datetime.now().timestamp() * 1000)
                topic = message.topic
                event = deserialize(message.value, self.protobuf_format)
                queue.put((timestamp, topic, event))

        except NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")

    def _start_processing(self, queue: Queue):
        while True:
            timestamp, topic, event = queue.get()
            if details := unpack_event_details(topic, event):
                if geocell := self._latency_markers.check_latency_marker(details):
                    if topic.startswith("input."):
                        self._process_ingress(geocell, event, timestamp)
                    elif topic.startswith("analytics."):
                        self._process_egress(topic, geocell, event, timestamp)

    def _process_ingress(self, geocell: int, event: Event, timestamp: int):
        marker = LatencyMarker()
        marker.mark_ingress(event, timestamp)
        self.seen_markers[geocell] = marker

    def _process_egress(self, topic: str, geocell: int, event: Event, timestamp: int):
        if geocell in self.seen_markers:
            job = topic[10:]
            marker = self.seen_markers[geocell]

            # Ensure that each marker is observed only once for each job
            # Otherwise they would appear with increasing latency when being contained in multiple sliding windows
            if not job in marker.seen_jobs:
                logger.debug(f"Recording egress marker for {job}")
                marker.mark_egress(event, job, timestamp)
                self.recorder.write(marker)
