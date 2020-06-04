import functools
import json
import logging
import datetime
from typing import List, final

from dateutil.parser import isoparse
import paho.mqtt.client as mqtt
from kafka import KafkaProducer

from ptinsight.event import Event
from ptinsight.util import match_dict_path


logger = logging.getLogger(__name__)


class Ingestor:

    _producer: KafkaProducer

    def __init__(self):
        pass

    def start(self):
        pass

    @staticmethod
    def create_producer(config: dict):
        Ingestor._producer = KafkaProducer(**config)

    @final
    def _ingest(self, topic: str, event: Event):
        logger.info(f"Ingesting event to {topic}")
        json_repr = json.dumps(event.to_dict())
        Ingestor._producer.send(topic, json_repr.encode())


class MQTTIngestor(Ingestor):
    def __init__(self, host: str, port: int, streams: List[dict]):
        super().__init__()
        self.host = host
        self.port = port
        self.streams = streams

        self.client = mqtt.Client()
        self.client.enable_logger(logger)
        self.client.on_connect = self._mqtt_on_connect
        self.client.on_message = self._mqtt_on_message
        if port == 8883:
            self.client.tls_set()

    def start(self):
        logger.info(f"Starting MQTT ingestor({self.host}:{self.port})")

        self.client.connect(self.host, self.port, keepalive=60)
        self.client.loop_forever()

    def _mqtt_on_connect(self, client, userdata, flags, rc):
        for stream in self.streams:
            client.subscribe(stream["topic"])

    @functools.lru_cache(256)
    def _match_stream(self, topic) -> dict:
        for stream in self.streams:
            if mqtt.topic_matches_sub(stream["topic"], topic):
                return stream

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        ingestion_timestamp = datetime.datetime.now(datetime.timezone.utc).replace(
            microsecond=0
        )

        stream = self._match_stream(msg.topic)
        if not stream:
            logger.debug("Unknown topic")

        full_payload = json.loads(msg.payload)

        # select_fields: select subset of fields from payload
        if not "select_fields" in stream:
            payload = full_payload
        else:
            payload = {}
            for field in stream["select_fields"]:
                payload.update(match_dict_path(full_payload, field))

        # static_fields: add static fields to payload
        if "select_fields" in stream:
            for field, value in stream["static_fields"].items():
                payload[field] = value

        if "event_timestamp" in stream:
            event_timestamp = isoparse(
                list(match_dict_path(full_payload, stream["event_timestamp"]).values())[
                    0
                ]
            )
        else:
            event_timestamp = ingestion_timestamp
        event = Event(event_timestamp, ingestion_timestamp, payload)

        self._ingest(stream["target"], event)
