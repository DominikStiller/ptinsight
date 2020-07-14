import abc
import base64
import functools
import json
import logging
import re
import sched
import threading
import time
from datetime import datetime, timezone
from typing import final, Literal

import boto3
import paho.mqtt.client as mqtt
from google.protobuf.message import Message
from kafka import KafkaProducer
from ptinsight.common import Event
from ptinsight.common.serialize import serialize

from ptinsight.ingest.processors import MQTTProcessor, Processor

logger = logging.getLogger(__name__)


class Ingestor(abc.ABC):
    """Receives messages and publishes them to Kafka after transforming using a Processor"""

    _protobuf_format = "binary"
    _producer = None

    @abc.abstractmethod
    def __init__(self, config: dict, processor: Processor):
        pass

    @abc.abstractmethod
    def start(self):
        """Starts the ingestor"""
        pass

    @classmethod
    @final
    def setup_producer(cls, producer_type: Literal["kafka", "console"], config: dict):
        if "protobuf_format" in config:
            Ingestor._protobuf_format = config.pop("protobuf_format")
        if producer_type == "kafka":
            cls._producer = KafkaProducer(**config)
        elif producer_type == "console":
            cls._producer = cls._ConsoleProducer()
        else:
            raise ValueError("Invalid producer type")

    class _ConsoleProducer:
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
    """Receives messages from an MQTT broker"""

    def __init__(self, config: dict, processor: MQTTProcessor):
        super().__init__(config, processor)
        self.host = config["host"]
        self.port = int(config["port"])
        self.processor = processor

        self.client = mqtt.Client()
        self.client.enable_logger(logger)
        self.client.on_connect = self._mqtt_on_connect
        self.client.on_message = self._mqtt_on_message
        if self.port == 8883:
            self.client.tls_set()

    def start(self):
        logger.info(f"Starting MQTT ingestor({self.host}:{self.port})")

        self.client.connect(self.host, self.port, keepalive=60)
        self.client.loop_forever()

    def _mqtt_on_connect(self, client, userdata, flags, rc):
        for topic in self.processor.topics:
            client.subscribe(topic)

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        ingestion_timestamp = datetime.now(timezone.utc)

        if processed := self.processor.process(msg.topic, json.loads(msg.payload)):
            target_topic, event_timestamp, details = processed

            event = Event()
            event.event_timestamp.FromDatetime(event_timestamp or ingestion_timestamp)
            event.ingestion_timestamp.FromDatetime(ingestion_timestamp)
            event.details.Pack(details)

            self._ingest(target_topic, event)


class MQTTRecordingIngestor(Ingestor):
    """Receives messages from an MQTT recording located in S3"""

    def __init__(self, config: dict, processor: MQTTProcessor):
        super().__init__(config, processor)
        self.config = config
        self.bucket = config["bucket"]
        self.key = config["key"]
        self.processor = processor

        self.file = boto3.resource("s3").Object(self.bucket, self.key)

    def start(self):
        logger.info(f"Starting MQTT recording ingestor(s3://{self.bucket}/{self.key})")

        lines = map(lambda l: l.decode(), self.file.get()["Body"].iter_lines())

        original_broker = next(lines)[8:]
        original_topics = next(lines)[8:]
        original_t_start = datetime.fromisoformat(next(lines)[12:]).replace(
            microsecond=0
        )
        next(lines)

        logger.info(f"Recorded from {original_broker} at {str(original_t_start)}")
        logger.info(f"Topics: {original_topics}\n")

        self._start_latency_marker_generator()
        self._start_scheduler(lines)

    def _start_scheduler(self, lines):
        # Scheduler is used to replay messages with original relative timing
        scheduler = sched.scheduler(time.perf_counter, time.sleep)
        done = False

        # Custom run method is necessary to prevent scheduler from exiting early because no events are scheduled yet
        def run_scheduler():
            while not done:
                scheduler.run()

        thread = threading.Thread(target=run_scheduler)
        thread.start()
        t_start = time.perf_counter()

        message_regex = re.compile(r'(\S+) "(.+)" (\d) (\d) (\S*)')
        for line in lines:
            # Prevent queue from growing too fast
            if len(scheduler._queue) > 1000:
                time.sleep(0.5)
                continue

            t_offset, topic, _, _, payload = message_regex.match(line).groups()
            t_offset = float(t_offset)
            payload = base64.b64decode(payload)

            scheduler.enterabs(
                t_start + t_offset,
                1,
                functools.partial(self._process_and_ingest, topic, payload),
            )

        done = True
        thread.join()

    def _start_latency_marker_generator(self):
        if "latency_marker_interval" in self.config:
            interval = int(self.config["latency_marker_interval"]) / 1000
            if interval <= 0:
                return

            def _run():
                threading.Thread(target=self._emit_latency_markers).start()
                threading.Timer(interval, _run).start()

            t = threading.Timer(interval, _run)
            t.setDaemon(False)
            t.start()

    def _emit_latency_markers(self):
        ingestion_timestamp = datetime.now(timezone.utc)

        markers = self.processor.generate_latency_markers()
        for target_topic, event_timestamp, details in markers:
            self._ingest(
                target_topic,
                self._create_event(ingestion_timestamp, event_timestamp, details),
            )

    def _process_and_ingest(self, topic: str, payload: str):
        ingestion_timestamp = datetime.now(timezone.utc)

        if processed := self.processor.process(topic, json.loads(payload)):
            target_topic, event_timestamp, details = processed
            self._ingest(
                target_topic,
                self._create_event(ingestion_timestamp, event_timestamp, details),
            )

    def _create_event(
        self, ingestion_timestamp: datetime, event_timestamp: datetime, details: Message
    ):
        event = Event()

        event.event_timestamp.FromDatetime(event_timestamp or ingestion_timestamp)
        event.ingestion_timestamp.FromDatetime(ingestion_timestamp)
        event.details.Pack(details)

        return event
