from datetime import datetime, timedelta
from random import randint
from typing import List, Tuple

from google.protobuf.message import Message

from ptinsight.common import VehicleType
from ptinsight.common.geocells import SpiralingGeocellGenerator
from ptinsight.common.proto.ingress.hsl_realtime_pb2 import (
    VehiclePosition,
    Arrival,
    Departure,
)


class LatencyMarker:
    def __init__(self, origin: Tuple[float, float], h3_resolution: int):
        self.coordinate_generator = SpiralingGeocellGenerator(
            origin, h3_resolution
        ).coordinates()

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
                    self._generate_vehicle_position_latency_marker()
                ),
            ),
            (
                "ingress.arrival",
                timestamp,
                _add_common_information(self._generate_arrival_latency_marker()),
            ),
            (
                "ingress.departure",
                timestamp,
                _add_common_information(self._generate_departure_latency_marker()),
            ),
        ]

    def _generate_vehicle_position_latency_marker(self) -> VehiclePosition:
        event = VehiclePosition()

        event.route.id = str(randint(100000, 2 ** 31 - 1))
        event.route.direction = 1
        event.route.operating_day = self._latest_timestamp.strftime("%Y-%m-%d")
        event.route.departure_time = (
            self._latest_timestamp - timedelta(minutes=20)
        ).strftime("%H:%M")
        event.heading = 0
        event.speed = 10
        event.acceleration = 3

        return event

    def _generate_arrival_latency_marker(self) -> Arrival:
        event = Arrival()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(self._latest_timestamp)
        event.scheduled_departure.FromDatetime(
            self._latest_timestamp + timedelta(minutes=1)
        )

        return event

    def _generate_departure_latency_marker(self) -> Departure:
        event = Departure()

        event.stop = randint(100000, 2 ** 31 - 1)
        event.scheduled_arrival.FromDatetime(self._latest_timestamp)
        event.scheduled_departure.FromDatetime(
            self._latest_timestamp + timedelta(minutes=1)
        )

        return event
