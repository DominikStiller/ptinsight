from __future__ import annotations

import abc
import functools
import itertools
from datetime import datetime
from typing import Tuple, Optional, List

import paho.mqtt.client as mqtt
from dateutil.parser import isoparse
from google.protobuf.message import Message
from ptinsight.common import Arrival, Departure, VehiclePosition, VehicleType
from ptinsight.common.hslrealtime import LatencyMarker


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
        self, source: str, payload: dict
    ) -> Optional[Tuple[str, datetime, Message]]:
        """
        Filters and transforms message from the raw format and the internal event format

        Args:
            source: The source of the message, e.g., an MQTT topic
            payload: The payload that contains the event data

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
        self.config = config
        self.event_types = config["event_types"].split(",")
        self.vehicle_types = config["vehicle_types"].split(",")
        self._latest_timestamp = None
        self._latency_markers = None

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
        self, source: str, payload: dict
    ) -> Optional[Tuple[str, datetime, Message]]:
        vehicle_type = self._get_vehicle_type(source)
        if not vehicle_type:
            return

        event_type = list(payload.keys())[0].lower()
        payload = list(payload.values())[0]
        event_timestamp = isoparse(payload["tst"])
        if not self._latest_timestamp or event_timestamp > self._latest_timestamp:
            self._latest_timestamp = event_timestamp

        if not payload["lat"] or not payload["long"]:
            return

        if event_type == "ars":
            target_topic = "ingress.arrival"
            event = Arrival()

            event.stop = int(payload["stop"])
            event.scheduled_arrival.FromJsonString(payload["ttarr"])
            event.scheduled_departure.FromJsonString(payload["ttdep"])
        elif event_type == "dep":
            target_topic = "ingress.departure"
            event = Departure()

            event.stop = int(payload["stop"])
            event.scheduled_arrival.FromJsonString(payload["ttarr"])
            event.scheduled_departure.FromJsonString(payload["ttdep"])
        elif event_type == "vp":
            target_topic = "ingress.vehicle-position"
            event = VehiclePosition()

            event.route.id = payload["route"]
            # Directions in realtime API are encoded by 1 or 2, but we want to use 0 or 1
            event.route.direction = bool(int(payload["dir"]) - 1)
            event.route.operating_day = payload["oday"]
            event.route.departure_time = payload["start"]
            if payload["hdg"]:
                event.heading = int(payload["hdg"])
            if payload["spd"]:
                event.speed = float(payload["spd"])
            if payload["acc"]:
                event.acceleration = float(payload["acc"])
        else:
            return

        event.latitude = float(payload["lat"])
        event.longitude = float(payload["long"])
        event.vehicle.type = VehicleType.Value(vehicle_type.upper())
        event.vehicle.operator = int(payload["oper"])
        event.vehicle.number = int(payload["veh"])

        return target_topic, event_timestamp, event

    def generate_latency_markers(self) -> List[Tuple[str, datetime, Message]]:
        if not self._latest_timestamp:
            # No real records have been processed yet
            return []
        if not self._latency_markers:
            # Use "Point Nemo" as origin since we can assume no real events come from there
            origin = (-48.875, -123.393)
            h3_resolution = int(self.config["h3_resolution"])
            self._latency_markers = LatencyMarker(origin, h3_resolution)
        return self._latency_markers.generate(self._latest_timestamp)
