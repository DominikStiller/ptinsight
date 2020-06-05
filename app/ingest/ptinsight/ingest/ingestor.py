import json
import logging
import datetime
import abc
from typing import final

import paho.mqtt.client as mqtt
from kafka import KafkaProducer

from ptinsight.event import Event
from ptinsight.ingest.connectors import MQTTConnector


logger = logging.getLogger(__name__)


class Ingestor(abc.ABC):

    _producer: KafkaProducer

    def __init__(self):
        pass

    @abc.abstractmethod
    def start(self):
        pass

    @staticmethod
    @final
    def create_producer(config: dict):
        Ingestor._producer = KafkaProducer(**config)

    @final
    def _ingest(self, topic: str, event: Event):
        logger.info(f"Ingesting event to {topic}")
        json_repr = json.dumps(event.to_dict())
        Ingestor._producer.send(topic, json_repr.encode())


class MQTTIngestor(Ingestor):
    def __init__(self, host: str, port: int, connector: MQTTConnector):
        super().__init__()
        self.host = host
        self.port = port
        self.connector = connector

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
        for topic in self.connector.topics:
            client.subscribe(topic)

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        ingestion_timestamp = datetime.datetime.now(datetime.timezone.utc).replace(
            microsecond=0
        )

        target_topic, event_timestamp, payload = self.connector.process(
            msg.topic, json.loads(msg.payload)
        )
        if not event_timestamp:
            event_timestamp = ingestion_timestamp

        event = Event(event_timestamp, ingestion_timestamp, payload)

        self._ingest(target_topic, event)
