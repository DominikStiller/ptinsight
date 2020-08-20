from typing import Optional

from google.protobuf.message import Message

from ptinsight.common import (
    Event,
    VehiclePosition,
    Arrival,
    Departure,
    VehicleCount,
    FinalStopCount,
    DelayStatistics,
    FlowDirection,
    EmergencyStop,
)


_TOPIC_TO_PROTOBUF_MAPPING = {
    "input.vehicle-position": VehiclePosition,
    "input.arrival": Arrival,
    "input.departure": Departure,
    "analytics.vehicle-count": VehicleCount,
    "analytics.delay-statistics": DelayStatistics,
    "analytics.flow-direction": FlowDirection,
    "analytics.final-stop-count": FinalStopCount,
    "analytics.emergency-stop-table": EmergencyStop,
    "analytics.emergency-stop-streaming": EmergencyStop,
}


def unpack_event_details(topic: str, event: Event) -> Optional[Message]:
    if not topic in _TOPIC_TO_PROTOBUF_MAPPING:
        return

    details = _TOPIC_TO_PROTOBUF_MAPPING[topic]()
    event.details.Unpack(details)

    return details
