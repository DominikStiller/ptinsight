import abc
import base64
import functools
import json
import logging
import multiprocessing
import re

import kafka
import sys
import threading
from multiprocessing import Process
from multiprocessing.managers import ValueProxy

import time
from datetime import datetime
from typing import final

import paho.mqtt.client as mqtt
from google.protobuf.message import Message
from ptinsight.common import Event
from ptinsight.common.logger import setup_logger
from ptinsight.common.recordings import open_recording
from ptinsight.common.scheduler import MultithreadingScheduler
from ptinsight.common.serialize import serialize

from ptinsight.ingest.processors import MQTTProcessor, Processor

logger = logging.getLogger(__name__)


class Ingestor(abc.ABC):
    """Receives messages and publishes them to Kafka after transforming using a Processor"""

    @abc.abstractmethod
    def __init__(self, config: dict, processor: Processor):
        self._protobuf_format = None
        self._producer = None

    @abc.abstractmethod
    def start(self):
        """Starts the ingestor"""
        pass

    @final
    def set_producer(self, producer_class, producer_config, protobuf_format: str):
        self._producer_class = producer_class
        self._producer_config = producer_config
        self._protobuf_format = protobuf_format

    @final
    def _create_producer(self):
        """Producers need to be created lazily to because Kafka producers does not support pickling for multiprocessing"""
        try:
            self._producer = self._producer_class(**self._producer_config)
        except kafka.errors.NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")
            sys.exit(1)

    @final
    def _ingest(self, topic: str, event: Event):
        logger.debug(f"Ingesting event to {topic}")

        value = serialize(event, format=self._protobuf_format)
        if isinstance(value, str):
            value = value.encode()

        self._producer.send(topic, value)


class _ConsoleProducer:
    """Drop-in replacement for Kafka producer for debugging purposes"""

    def __init__(self, **kwargs):
        pass

    def send(self, topic, value):
        print(f"{topic}: {value}")


class MQTTIngestor(Ingestor):
    """Receives messages from an MQTT broker"""

    def __init__(self, config: dict, processor: MQTTProcessor):
        super().__init__(config, processor)
        self.config = config
        self.host = config["host"]
        self.port = int(config["port"])
        self.processor = processor
        self.processor.set_latest_timestamp_valueproxy(multiprocessing.Value("f", -1))

        self.client = mqtt.Client()
        self.client.enable_logger(logger)
        self.client.on_connect = self._mqtt_on_connect
        self.client.on_message = self._mqtt_on_message
        if self.port == 8883:
            self.client.tls_set()

    def start(self):
        logger.info(f"Starting MQTT ingestor({self.host}:{self.port})")

        self._create_producer()

        _start_latency_marker_generator(
            self.config, self.processor.generate_latency_markers, self._ingest
        )

        self.client.connect(self.host, self.port, keepalive=60)
        self.client.loop_forever()

    def _mqtt_on_connect(self, client, userdata, flags, rc):
        for topic in self.processor.topics:
            client.subscribe(topic)

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        if processed := self.processor.process(msg.topic, json.loads(msg.payload)):
            target_topic, event_timestamp, details = processed
            self._ingest(target_topic, _create_event(event_timestamp, details))


class MQTTRecordingIngestor(Ingestor):
    """Receives messages from an MQTT recording located in S3"""

    def __init__(self, config: dict, processor: MQTTProcessor):
        super().__init__(config, processor)
        self.config = config
        self.bucket = config["bucket"]
        self.key = config["key"]
        self.processor = processor

    def start(self):
        header_body, header_lines = open_recording(self.bucket, self.key)
        original_broker = next(header_lines)[8:]
        original_topics = next(header_lines)[8:]
        original_t_start = datetime.fromisoformat(next(header_lines)[12:]).replace(
            microsecond=0
        )
        header_body.close()

        logger.info(f"Recorded from {original_broker} at {str(original_t_start)}")
        logger.info(f"Topics: {original_topics}\n")
        logger.info(f"Starting MQTT recording ingestor(s3://{self.bucket}/{self.key})")

        self._start_schedulers()

    def _start_schedulers(self):
        if "volume_scaling_factor" in self.config:
            scaling_factor = int(self.config["volume_scaling_factor"])
            if scaling_factor < 1:
                raise ValueError("Volume scale must be an integer >= 1")
        else:
            scaling_factor = 1

        if "volume_scaling_offset_lines" in self.config:
            scaling_offset = int(self.config["volume_scaling_offset_lines"])
            if scaling_offset < 0:
                raise ValueError("Volume scaling offset must be a positive integer")
        else:
            scaling_offset = 0

        logger.info(
            f"Scaling with factor {scaling_factor} and offset {scaling_offset} lines each"
        )

        with multiprocessing.Manager() as manager:
            skip_offset_barrier = manager.Barrier(scaling_factor)
            latest_timestamp = multiprocessing.Value("d")

            pool = []
            for scheduler_index in range(scaling_factor):
                p = Process(
                    target=self._start_single_scheduler,
                    args=(
                        scheduler_index,
                        scheduler_index * scaling_offset,
                        skip_offset_barrier,
                        latest_timestamp,
                    ),
                )
                pool.append(p)
                p.start()

            multiprocessing.connection.wait(p.sentinel for p in pool)

    def _start_single_scheduler(
        self,
        scheduler_index: int,
        offset: int,
        skip_offset_barrier: multiprocessing.Barrier,
        latest_timestamp: ValueProxy,
    ):
        setup_logger("info")
        self._create_producer()
        self.processor.set_latest_timestamp_valueproxy(latest_timestamp)

        _, lines = open_recording(self.bucket, self.key)

        # Skip header
        for _ in range(4):
            next(lines)

        # "Fast-forward" recording to offset
        for _ in range(offset):
            next(lines)

        logger.info(
            f"Scheduler {scheduler_index+1} of {skip_offset_barrier.parties} ready"
        )
        skip_offset_barrier.wait()

        # Scheduler is used to replay messages with original relative timing
        scheduler = MultithreadingScheduler()
        scheduler.start()

        if scheduler_index == 0:
            _start_latency_marker_generator(
                self.config, self.processor.generate_latency_markers, self._ingest
            )

        # Necessary to start immediately despite "fast-forwarding"
        recording_start_offset = None

        message_regex = re.compile(r'(\S+) "(.+)" (\d) (\d) (\S*)')
        for i, line in enumerate(lines):
            # Prevent queue from growing too fast
            # Only check every 100 iterations for performance reasons
            if i % 100 == 0 and scheduler.is_queue_full():
                time.sleep(0.2)
                continue

            t_offset, topic, _, _, payload = message_regex.match(line).groups()
            t_offset = float(t_offset)
            payload = json.loads(base64.b64decode(payload))

            if recording_start_offset is None:
                recording_start_offset = t_offset

            scheduler.schedule(
                t_offset - recording_start_offset,
                functools.partial(
                    self._process_and_ingest, topic, payload, scheduler_index
                ),
            )

        scheduler.stop()

    def _process_and_ingest(self, topic: str, payload: dict, scheduler_index: int):
        if processed := self.processor.process(topic, payload, scheduler_index):
            target_topic, event_timestamp, details = processed
            self._ingest(
                target_topic, _create_event(event_timestamp, details),
            )


def _start_latency_marker_generator(config: dict, generator_fn, ingest_fn):
    if "latency_marker_interval" in config:
        interval = int(config["latency_marker_interval"]) / 1000
        if interval <= 0:
            return

        def _run():
            def _emit_latency_markers():
                markers = generator_fn()
                for target_topic, event_timestamp, details in markers:
                    ingest_fn(
                        target_topic, _create_event(event_timestamp, details),
                    )

            threading.Thread(target=_emit_latency_markers).start()
            threading.Timer(interval, _run).start()

        t = threading.Timer(interval, _run)
        t.setDaemon(False)
        t.start()


def _create_event(event_timestamp: float, details: Message):
    event = Event()

    if event_timestamp:
        event.event_timestamp.FromMilliseconds(int(event_timestamp * 1000))
    event.ingestion_timestamp.GetCurrentTime()
    event.details.Pack(details)

    return event
