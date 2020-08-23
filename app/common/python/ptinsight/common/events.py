from typing import Optional

from google.protobuf.message import Message

from ptinsight.common import (
    Event,
    VehiclePosition,
    Arrival,
    Departure,
    VehicleDistributionResult,
    FinalStopDistributionResult,
    DelayDistributionResult,
    FlowDirectionResult,
    EmergencyStopDetectionResult,
)


_TOPIC_TO_PROTOBUF_MAPPING = {
    "input.vehicle-position": VehiclePosition,
    "input.arrival": Arrival,
    "input.departure": Departure,
    "analytics.vehicle-distribution": VehicleDistributionResult,
    "analytics.delay-distribution": DelayDistributionResult,
    "analytics.flow-direction": FlowDirectionResult,
    "analytics.final-stop-distribution": FinalStopDistributionResult,
    "analytics.emergency-stop-detection-table": EmergencyStopDetectionResult,
    "analytics.emergency-stop-detection-streaming": EmergencyStopDetectionResult,
}


def unpack_event_details(topic: str, event: Event) -> Optional[Message]:
    if not topic in _TOPIC_TO_PROTOBUF_MAPPING:
        return

    details = _TOPIC_TO_PROTOBUF_MAPPING[topic]()
    event.details.Unpack(details)

    return details
