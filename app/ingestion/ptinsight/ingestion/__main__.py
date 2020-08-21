import logging
import os
from concurrent.futures import wait

import sys
from concurrent.futures.thread import ThreadPoolExecutor

import yaml
from kafka import KafkaProducer

from ptinsight.common.logger import setup_logger
from ptinsight.ingestion.connector import (
    MQTTConnector,
    MQTTRecordingConnector,
    _ConsoleProducer,
)
from ptinsight.ingestion.processors import MQTTProcessor

logger = logging.getLogger(__name__)


# Load config
if os.path.exists("config/ingestion.yaml"):
    config_path = "config/ingestion.yaml"
elif os.path.exists("config/ingestion.default.yaml"):
    config_path = "config/ingestion.default.yaml"
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

# Load sources from config and create respective connectors
connectors = []
for source in config["connectors"]:
    connector_config = source["config"]
    if source["type"] == "mqtt":
        connector_class = MQTTConnector
    elif source["type"] == "mqtt-recording":
        connector_class = MQTTRecordingConnector
    else:
        continue

    processor_type = source["processor"]["type"]
    processor_config = {**source["processor"]["config"], "h3": {**config["h3"]}}
    processor = processors[processor_type](processor_config)

    connector = connector_class(connector_config, processor)
    connector.set_producer(producer_class, producer_config, protobuf_format)
    connectors.append(connector)

# Start all connectors
with ThreadPoolExecutor() as executor:
    connectors[0].start()
    # wait([executor.submit(connector.start) for connector in connectors])
