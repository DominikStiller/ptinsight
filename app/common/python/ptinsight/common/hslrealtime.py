from datetime import datetime, timedelta
from random import randint
from typing import List, Tuple, Optional

import h3.api.basic_int as h3
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

    def parse(
        self, vehicle_type: str, payload: dict
    ) -> Optional[Tuple[str, datetime, Message]]:
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

    # Use special operator for ingress latency markers
    LATENCY_MARKER_OPERATOR = 42000

    def __init__(self, origin: Tuple[float, float], h3_resolution: int, h3_max_k: int):
        self.origin = h3.geo_to_h3(*origin, h3_resolution)
        self.h3_resolution = h3_resolution
        self.h3_max_k = h3_max_k

        self.coordinate_generator = SpiralingGeocellGenerator(
            self.origin, h3_max_k
        ).coordinates()

    def check_latency_marker(self, event: Message) -> Optional[int]:
        """
        Checks if a message is a latency marker

        Args:
            event: The event details protobuf message

        Returns:
            The geocell if the message is a latency marker, None otherwise
        """
        # Extract geocell first
        if hasattr(event, "geocell"):
            cell = event.geocell
        elif hasattr(event, "latitude") and hasattr(event, "longitude"):
            cell = h3.geo_to_h3(event.latitude, event.longitude, self.h3_resolution)
        else:
            return

        if hasattr(event, "vehicle") and hasattr(event.vehicle, "operator"):
            # For ingress events, we can check for the special operator
            if event.vehicle.operator == self.LATENCY_MARKER_OPERATOR:
                return cell
        else:
            # For egress events, we need to check by geocell distance
            try:
                if h3.h3_distance(self.origin, cell) <= self.h3_max_k:
                    return cell
            except SystemError:
                # System errors can occur when the distance is too large
                pass

    def generate(self, timestamp: datetime) -> List[Tuple[str, datetime, Message]]:
        def _add_common_information(event: Message) -> Message:
            coordinates = next(self.coordinate_generator)
            event.latitude = coordinates[0]
            event.longitude = coordinates[1]

            event.vehicle.type = VehicleType.BUS
            event.vehicle.operator = self.LATENCY_MARKER_OPERATOR
            event.vehicle.number = randint(100000, 2 ** 31 - 1)

            return event

        markers = [
            (
                "ingress.vehicle-position",
                self._generate_vehicle_position_latency_marker(timestamp),
            ),
            ("ingress.arrival", self._generate_arrival_latency_marker(timestamp)),
            ("ingress.departure", self._generate_departure_latency_marker(timestamp)),
        ]

        return [
            (topic, timestamp, _add_common_information(event))
            for topic, event in markers
        ]

    def _generate_vehicle_position_latency_marker(
        self, timestamp: datetime
    ) -> VehiclePosition:
        event = VehiclePosition()

        event.route.id = str(randint(100000, 2 ** 31 - 1))
        event.route.direction = 1
        event.route.operating_day = timestamp.strftime("%Y-%m-%d")
        event.route.departure_time = (timestamp - timedelta(minutes=20)).strftime(
            "%H:%M"
        )
        event.heading = 0
        event.speed = 10
        event.acceleration = 3

        return event

    def _generate_arrival_latency_marker(self, timestamp: datetime) -> Arrival:
        event = Arrival()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(timestamp)
        event.scheduled_departure.FromDatetime(timestamp + timedelta(minutes=1))

        return event

    def _generate_departure_latency_marker(self, timestamp: datetime) -> Departure:
        event = Departure()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(timestamp)
        event.scheduled_departure.FromDatetime(timestamp + timedelta(minutes=1))

        return event
