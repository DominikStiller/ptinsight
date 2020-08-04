import logging
import os
from concurrent.futures import wait

import sys
from concurrent.futures.thread import ThreadPoolExecutor

import yaml
from kafka import KafkaProducer

from ptinsight.common.logger import setup_logger
from ptinsight.ingest.ingestor import (
    MQTTIngestor,
    MQTTRecordingIngestor,
    _ConsoleProducer,
)
from ptinsight.ingest.processors import MQTTProcessor

logger = logging.getLogger(__name__)


# Load config
if os.path.exists("config/ingest.yaml"):
    config_path = "config/ingest.yaml"
elif os.path.exists("config/ingest.default.yaml"):
    config_path = "config/ingest.default.yaml"
else:
    print("Config file not found")
    sys.exit(1)

with open(config_path) as f:
    config = yaml.safe_load(f)

setup_logger(config["logging"]["level"])

# Create console/Kafka producer depending on configuration
producer_config = config["producer"]
producer_type = producer_config.pop("type")

if "protobuf_format" in producer_config:
    protobuf_format = producer_config.pop("protobuf_format")
else:
    protobuf_format = "binary"

if producer_type == "kafka":
    producer_class = KafkaProducer
elif producer_type == "console":
    producer_class = _ConsoleProducer
else:
    print("Invalid producer type")
    sys.exit(1)

# Discover processor classes
processors = {}
for type in [MQTTProcessor]:
    # Only finds direct subclasses
    classes = type.__subclasses__()
    for cls in classes:
        processors[cls.name()] = cls

# Load sources from config and create respective ingestors
ingestors = []
for source in config["sources"]:
    ingestor_config = source["config"]
    if source["type"] == "mqtt":
        ingestor_class = MQTTIngestor
    elif source["type"] == "mqtt-recording":
        ingestor_class = MQTTRecordingIngestor
    else:
        continue

    processor_type = source["processor"]["type"]
    processor_config = {**source["processor"]["config"], "h3": {**config["h3"]}}
    processor = processors[processor_type](processor_config)

    ingestor = ingestor_class(ingestor_config, processor)
    ingestor.set_producer(producer_class, producer_config, protobuf_format)
    ingestors.append(ingestor)

# Start all ingestors
with ThreadPoolExecutor() as executor:
    wait([executor.submit(ingestor.start) for ingestor in ingestors])
