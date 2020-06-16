from __future__ import annotations

import abc
import functools
import itertools
from datetime import datetime
from typing import Tuple, Optional

import paho.mqtt.client as mqtt
from dateutil.parser import isoparse
from google.protobuf.message import Message
from ptinsight.common import Arrival, Departure, VehiclePosition, VehicleType


class Processor(abc.ABC):
    def __init__(self, config: dict):
        pass

    @staticmethod
    @abc.abstractmethod
    def name():
        pass

    @abc.abstractmethod
    def process(
        self, source: str, payload: dict
    ) -> Optional[Tuple[str, datetime, Message]]:
        pass


class MQTTProcessor(Processor, abc.ABC):
    @property
    @abc.abstractmethod
    def topics(self):
        pass


class HSLRealtimeProcessor(MQTTProcessor):
    def __init__(self, config):
        super().__init__(config)
        self.event_types = config["event_types"].split(",")
        self.vehicle_types = config["vehicle_types"].split(",")

    @staticmethod
    def name():
        return "hsl-realtime"

    @property
    def topics(self):
        return [
            f"/hfp/v2/journey/ongoing/{e}/{v}/#"
            for e, v in itertools.product(self.event_types, self.vehicle_types)
        ]

    @functools.lru_cache(256)
    def _get_vehicle_type(self, topic: str):
        for type in self.vehicle_types:
            if mqtt.topic_matches_sub(f"/hfp/v2/journey/ongoing/+/{type}/#", topic):
                return type

    def process(
        self, source: str, payload: dict
    ) -> Optional[Tuple[str, datetime, Message]]:
        vehicle_type = self._get_vehicle_type(source)
        if not vehicle_type:
            return

        event_type = list(payload.keys())[0].lower()
        payload = list(payload.values())[0]
        event_timestamp = isoparse(payload["tst"])

        if event_type == "arr":
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
            event.latitude = float(payload["lat"])
            event.longitude = float(payload["long"])
            event.heading = int(payload["hdg"])
            event.speed = float(payload["spd"])
            event.acceleration = float(payload["acc"])
        else:
            return

        event.vehicle.type = VehicleType.Value(vehicle_type.upper())
        event.vehicle.operator = int(payload["oper"])
        event.vehicle.number = int(payload["veh"])

        return target_topic, event_timestamp, event