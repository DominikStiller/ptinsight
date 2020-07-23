from __future__ import annotations

import abc
import functools

import itertools
from datetime import datetime
from typing import Tuple, Optional, List

import paho.mqtt.client as mqtt
from google.protobuf.message import Message
from ptinsight.common.hslrealtime import HSLRealtimeParser, HSLRealtimeLatencyMarkers


class Processor(abc.ABC):
    """Transforms received messages to the appropriate protobuf message for a Kafka topic"""

    def __init__(self, config: dict):
        pass

    @staticmethod
    @abc.abstractmethod
    def name():
        """Defines the name under which the processor can be referred to in the config file"""
        pass

    @abc.abstractmethod
    def process(
        self, source: str, payload: dict, i: int = 0
    ) -> Optional[Tuple[str, datetime, Message]]:
        """
        Filters and transforms message from the raw format and the internal event format

        Args:
            source: The source of the message, e.g., an MQTT topic
            payload: The payload that contains the event data
            i: The ascending number of the scheduler in case of volume scaling

        Returns:
            A tuple of the Kafka topic, the event time and the protobuf message, or None if the message should be dismissed
        """
        pass


class MQTTProcessor(Processor, abc.ABC):
    @property
    @abc.abstractmethod
    def topics(self):
        """Defines the topics an MQTT ingestor should subscribe to"""
        pass

    def generate_latency_markers(self) -> List[Tuple[str, datetime, Message]]:
        """
        Generates all latency markers messages

        Returns:
            A list of tuples of the Kafka topic, the event time and the protobuf message
        """
        pass


class HSLRealtimeProcessor(MQTTProcessor):
    def __init__(self, config):
        super().__init__(config)
        self.event_types = config["event_types"].split(",")
        self.vehicle_types = config["vehicle_types"].split(",")
        self._latest_timestamp = None
        self._parser = HSLRealtimeParser()

        # Use "Point Nemo" as latency marker origin since we can assume no real events come from there
        origin = (-48.875, -123.393)
        h3_resolution = int(config["h3"]["resolution"])
        h3_max_k = int(config["h3"]["max_k"])
        self._latency_markers = HSLRealtimeLatencyMarkers(
            origin, h3_resolution, h3_max_k
        )

    @staticmethod
    def name():
        return "hsl-realtime"

    @property
    def topics(self):
        return [
            f"/hfp/v2/journey/ongoing/{event}/{vehicle}/#"
            for event, vehicle in itertools.product(
                self.event_types, self.vehicle_types
            )
        ]

    @functools.lru_cache(256)
    def _get_vehicle_type(self, topic: str):
        # Make sure we only ingest desired event and vehicle types
        for event, vehicle in itertools.product(self.event_types, self.vehicle_types):
            if mqtt.topic_matches_sub(
                f"/hfp/v2/journey/ongoing/{event}/{vehicle}/#", topic
            ):
                return vehicle

    def process(
        self, source: str, payload: dict, i: int = 0
    ) -> Optional[Tuple[str, datetime, Message]]:
        vehicle_type = self._get_vehicle_type(source)
        if not vehicle_type:
            return

        if parsed := self._parser.parse(vehicle_type, payload):
            event_type, event_timestamp, event = parsed

            if i == 0:
                # Only use primary recording as timestamp source
                if (
                    not self._latest_timestamp
                    or event_timestamp > self._latest_timestamp
                ):
                    self._latest_timestamp = event_timestamp
            else:
                if not self._latest_timestamp:
                    return
                # Adjust non-primary payload to prevent collisions when scaling volume
                event_timestamp, event = self._parser.adjust_payload(
                    i, event, event_timestamp, self._latest_timestamp
                )

            target_topic = (
                "ingress."
                + {"ars": "arrival", "dep": "departure", "vp": "vehicle-position"}[
                    event_type
                ]
            )

            return target_topic, event_timestamp, event

    def generate_latency_markers(self) -> List[Tuple[str, datetime, Message]]:
        if not self._latest_timestamp:
            # No real records have been processed yet
            return []
        return self._latency_markers.generate(self._latest_timestamp)
