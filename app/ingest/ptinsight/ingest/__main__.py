import logging
import os
import sys
from concurrent.futures import wait
from concurrent.futures.thread import ThreadPoolExecutor

import kafka.errors
import yaml

from ptinsight.ingest.ingestor import MQTTIngestor, Ingestor, MQTTRecordingIngestor
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

logging.basicConfig()
logging.getLogger("ptinsight").setLevel(config["logging"]["level"].upper())

# Create console/Kafka producer depending on configuration
producer_config = config["producer"]
producer_type = producer_config.pop("type")
try:
    Ingestor.setup_producer(producer_type, producer_config)
except kafka.errors.NoBrokersAvailable:
    logger.error("Cannot connect to Kafka bootstrap servers")
    sys.exit(1)

# Find processor classes
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

    ingestors.append(ingestor_class(ingestor_config, processor))

# Start all ingestors
with ThreadPoolExecutor() as executor:
    wait([executor.submit(ingestor.start) for ingestor in ingestors])
