from typing import Union

from google.protobuf.json_format import MessageToJson, Parse

from ptinsight.common import Event

_json_opts = {
    "preserving_proto_field_name": True,
    "including_default_value_fields": True,
}


def serialize(event: Event, format="binary") -> Union[str, bytes]:
    if format == "binary":
        return event.SerializeToString()
    elif format == "json":
        return MessageToJson(event, indent=None, **_json_opts)
    elif format == "json-pretty":
        return MessageToJson(event, indent=2, **_json_opts)
    else:
        raise Exception("Unknown protobuf wire format")


def deserialize(event: bytes, format="binary") -> Event:
    if format == "binary":
        return Event.ParseFromString(event)
    elif format == "json" or format == "json-pretty":
        return Parse(event, Event())
    else:
        raise Exception("Unknown protobuf wire format")
