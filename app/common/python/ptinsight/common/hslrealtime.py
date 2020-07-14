from datetime import datetime, timedelta
from random import randint
from typing import List, Tuple, Optional

import h3
from google.protobuf.message import Message
from dateutil.parser import isoparse

from ptinsight.common import VehicleType
from ptinsight.common.geocells import SpiralingGeocellGenerator
from ptinsight.common.proto.ingress.hsl_realtime_pb2 import (
    VehiclePosition,
    Arrival,
    Departure,
)


class HSLRealtimeParser:
    """Parser for HSL realtime events"""
    def parse(self, vehicle_type: str, payload: dict) -> Optional[Tuple[str, datetime, Message]]:
        event_type = list(payload.keys())[0].lower()
        payload = list(payload.values())[0]
        event_timestamp = isoparse(payload["tst"])

        if not payload["lat"] or not payload["long"]:
            return

        if event_type == "ars":
            event = Arrival()

            event.stop = int(payload["stop"])
            event.scheduled_arrival.FromJsonString(payload["ttarr"])
            event.scheduled_departure.FromJsonString(payload["ttdep"])
        elif event_type == "dep":
            event = Departure()

            event.stop = int(payload["stop"])
            event.scheduled_arrival.FromJsonString(payload["ttarr"])
            event.scheduled_departure.FromJsonString(payload["ttdep"])
        elif event_type == "vp":
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

        return event_type, event_timestamp, event


class HSLRealtimeLatencyMarkers:
    """Latency marker generator for HSL realtime events"""
    def __init__(self, origin: Tuple[float, float], h3_resolution: int, h3_max_k: int):
        self.origin = origin
        self.h3_max_k = h3_max_k
        self.generator = SpiralingGeocellGenerator(
            origin, h3_resolution, h3_max_k
        )
        self.coordinate_generator = self.generator.coordinates()

    def is_latency_marker(self, cell: int) -> bool:
        return h3.h3_distance(self.origin, cell) <= self.h3_max_k

    def generate(self, timestamp: datetime) -> List[Tuple[str, datetime, Message]]:
        def _add_common_information(event):
            coordinates = next(self.coordinate_generator)
            event.latitude = coordinates[0]
            event.longitude = coordinates[1]

            event.vehicle.type = VehicleType.BUS
            event.vehicle.operator = 42000
            event.vehicle.number = randint(100000, 2 ** 31 - 1)

            return event

        return [
            (
                "ingress.vehicle-position",
                timestamp,
                _add_common_information(
                    self._generate_vehicle_position_latency_marker(timestamp)
                ),
            ),
            (
                "ingress.arrival",
                timestamp,
                _add_common_information(self._generate_arrival_latency_marker(timestamp)),
            ),
            (
                "ingress.departure",
                timestamp,
                _add_common_information(self._generate_departure_latency_marker(timestamp)),
            ),
        ]

    def _generate_vehicle_position_latency_marker(self, timestamp: datetime) -> VehiclePosition:
        event = VehiclePosition()

        event.route.id = str(randint(100000, 2 ** 31 - 1))
        event.route.direction = 1
        event.route.operating_day = timestamp.strftime("%Y-%m-%d")
        event.route.departure_time = (
            timestamp - timedelta(minutes=20)
        ).strftime("%H:%M")
        event.heading = 0
        event.speed = 10
        event.acceleration = 3

        return event

    def _generate_arrival_latency_marker(self, timestamp: datetime) -> Arrival:
        event = Arrival()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(timestamp)
        event.scheduled_departure.FromDatetime(
            timestamp + timedelta(minutes=1)
        )

        return event

    def _generate_departure_latency_marker(self, timestamp: datetime) -> Departure:
        event = Departure()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(timestamp)
        event.scheduled_departure.FromDatetime(
            timestamp + timedelta(minutes=1)
        )

        return event
