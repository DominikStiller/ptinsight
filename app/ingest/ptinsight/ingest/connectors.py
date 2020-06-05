from __future__ import annotations
import abc
import functools
from datetime import datetime
import itertools
from typing import Tuple

import paho.mqtt.client as mqtt
from dateutil.parser import isoparse


class Connector(abc.ABC):
    def __init__(self, config: dict):
        pass

    @staticmethod
    @abc.abstractmethod
    def name():
        pass

    @abc.abstractmethod
    def process(self, source: str, payload: dict) -> Tuple[str, datetime, dict]:
        pass


class MQTTConnector(Connector, abc.ABC):
    @property
    @abc.abstractmethod
    def topics(self):
        pass


class HSLRealtimeConnector(MQTTConnector):
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

    def process(self, source: str, payload: dict) -> Tuple[str, datetime, dict]:
        vehicle_type = self._get_vehicle_type(source)
        event_type = list(payload.keys())[0]

        target_topic = {
            "ARR": "ingress.arrival",
            "DEP": "ingress.departure",
            "VP": "ingress.vehicle-position",
        }[event_type]

        payload = payload[event_type]
        event_timestamp = isoparse(payload["tst"])
        del payload["tst"]
        del payload["tsi"]

        payload["vt"] = vehicle_type

        return target_topic, event_timestamp, payload
