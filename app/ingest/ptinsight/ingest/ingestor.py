import abc
import datetime
import json
import logging
from typing import final

import paho.mqtt.client as mqtt
from kafka import KafkaProducer
from ptinsight.common import Event
from ptinsight.common.serialize import serialize

from ptinsight.ingest.processors import MQTTProcessor

logger = logging.getLogger(__name__)


class Ingestor(abc.ABC):

    _protobuf_format = "binary"
    _producer = None

    def __init__(self):
        pass

    @abc.abstractmethod
    def start(self):
        pass

    @classmethod
    @final
    def create_kafka_producer(cls, config: dict):
        if "protobuf_format" in config:
            Ingestor._protobuf_format = config.pop("protobuf_format")
        cls._producer = KafkaProducer(**config)

    @classmethod
    @final
    def create_debug_producer(cls, config: dict):
        if "protobuf_format" in config:
            Ingestor._protobuf_format = config.pop("protobuf_format")
        cls._producer = cls._DebugProducer()

    class _DebugProducer:
        def send(self, topic, value):
            print(f"{topic}: {value}")

    @final
    def _ingest(self, topic: str, event: Event):
        logger.info(f"Ingesting event to {topic}")

        value = serialize(event, format=self._protobuf_format)
        if isinstance(value, str):
            value = value.encode()

        Ingestor._producer.send(topic, value)


class MQTTIngestor(Ingestor):
    def __init__(self, host: str, port: int, processor: MQTTProcessor):
        super().__init__()
        self.host = host
        self.port = port
        self.processor = processor

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
        for topic in self.processor.topics:
            client.subscribe(topic)

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        ingestion_timestamp = datetime.datetime.now(datetime.timezone.utc)

        # print(json.dumps(json.loads(msg.payload), indent=2))
        # self.client.disconnect()

        if processed := self.processor.process(msg.topic, json.loads(msg.payload)):
            target_topic, event_timestamp, details = processed

            event = Event()
            event.event_timestamp.FromDatetime(event_timestamp or ingestion_timestamp)
            event.ingestion_timestamp.FromDatetime(ingestion_timestamp)
            event.details.Pack(details)

            self._ingest(target_topic, event)
