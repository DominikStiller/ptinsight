import abc
import base64
import bz2
import functools
import json
import logging
import re
import sched
import threading
from concurrent.futures import wait
from concurrent.futures.thread import ThreadPoolExecutor

import time
from datetime import datetime
from typing import final, Literal, Union

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
        logger.debug(f"Ingesting event to {topic}")

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

        self.file = boto3.resource("s3").Object(self.bucket, self.key)
        self.skip_offset_barrier = None

    def start(self):
        header_body, header_lines = self._open_recording()
        original_broker = next(header_lines)[8:]
        original_topics = next(header_lines)[8:]
        original_t_start = datetime.fromisoformat(next(header_lines)[12:]).replace(
            microsecond=0
        )
        header_body.close()

        logger.info(f"Recorded from {original_broker} at {str(original_t_start)}")
        logger.info(f"Topics: {original_topics}\n")
        logger.info(f"Starting MQTT recording ingestor(s3://{self.bucket}/{self.key})")

        self._start_latency_marker_generator()
        self._start_schedulers()

    def _open_recording(self):
        body = self.file.get()["Body"]
        if self.key.endswith(".bz2"):
            return body, self._bz2_iter_lines(body)
        else:
            return body, map(lambda l: l.decode(), body.iter_lines())

    def _bz2_iter_lines(self, file):
        bz2_file = bz2.open(file)
        while line := bz2_file.readline():
            line = line.decode()
            if line.endswith("\r\n"):
                yield line[:-2]
            elif line.endswith("\n") or line.endswith("\r"):
                yield line[:-1]

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
            f"Scaling factor: {scaling_factor}   Scaling offset: {scaling_offset} lines"
        )

        self.skip_offset_barrier = threading.Barrier(scaling_factor)
        with ThreadPoolExecutor(scaling_factor) as executor:
            wait(
                [
                    executor.submit(self._start_single_scheduler, i, i * scaling_offset)
                    for i in range(scaling_factor)
                ]
            )

    def _start_single_scheduler(self, i: int, offset: int):
        _, lines = self._open_recording()

        # Skip header and partial lines caused by offset
        for _ in range(4):
            next(lines)

        for _ in range(offset):
            next(lines)

        logger.info(f"Scheduler {i+1} of {self.skip_offset_barrier.parties} ready")
        self.skip_offset_barrier.wait()

        # Scheduler is used to replay messages with original relative timing
        scheduler = sched.scheduler(time.perf_counter, time.sleep)
        done = threading.Event()

        # Custom run method is necessary to prevent scheduler from exiting early because no events are scheduled yet
        def run_scheduler():
            while not done.is_set():
                scheduler.run()
            # Clear queue after done is set
            scheduler.run()

        # One thread reads the recording, the other publishes the messages
        sched_thread = threading.Thread(target=run_scheduler)
        sched_thread.start()
        t_start = time.perf_counter()
        recording_start_offset = None

        message_regex = re.compile(r'(\S+) "(.+)" (\d) (\d) (\S*)')
        for line in lines:
            # Prevent queue from growing too fast
            if len(scheduler._queue) > 1000:
                time.sleep(0.5)
                continue

            t_offset, topic, _, _, payload = message_regex.match(line).groups()
            t_offset = float(t_offset)
            payload = json.loads(base64.b64decode(payload))

            if recording_start_offset is None:
                recording_start_offset = t_offset

            scheduler.enterabs(
                t_start + t_offset - recording_start_offset,
                1,
                functools.partial(self._process_and_ingest, topic, payload, i),
            )

        done.set()
        sched_thread.join()

    def _process_and_ingest(self, topic: str, payload: dict, i: int):
        if processed := self.processor.process(topic, payload, i):
            target_topic, event_timestamp, details = processed
            self._ingest(
                target_topic, _create_event(event_timestamp, details),
            )

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
        markers = self.processor.generate_latency_markers()
        for target_topic, event_timestamp, details in markers:
            self._ingest(
                target_topic, _create_event(event_timestamp, details),
            )


def _create_event(event_timestamp: datetime, details: Message):
    event = Event()

    if event_timestamp:
        event.event_timestamp.FromDatetime(event_timestamp)
    event.ingestion_timestamp.GetCurrentTime()
    event.details.Pack(details)

    return event
