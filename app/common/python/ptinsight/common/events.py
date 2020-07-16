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
    "ingress.vehicle-position": VehiclePosition,
    "ingress.arrival": Arrival,
    "ingress.departure": Departure,
    "egress.vehicle-count": VehicleCount,
    "egress.delay-statistics": DelayStatistics,
    "egress.flow-direction": FlowDirection,
    "egress.final-stop-count": FinalStopCount,
    "egress.emergency-stop-table": EmergencyStop,
    "egress.emergency-stop-streaming": EmergencyStop,
}


def unpack_event_details(topic: str, event: Event) -> Optional[Message]:
    if not topic in _TOPIC_TO_PROTOBUF_MAPPING:
        return

    details = _TOPIC_TO_PROTOBUF_MAPPING[topic]()
    event.details.Unpack(details)

    return details
