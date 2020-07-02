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
    if source["type"] == "mqtt":
        broker = source["broker"]
        processor = processors[source["processor"]](source["config"])
        ingestor = MQTTIngestor(broker["host"], int(broker["port"]), processor)
        ingestors.append(ingestor)
    elif source["type"] == "mqtt-recording":
        file = source["file"]
        processor = processors[source["processor"]](source["config"])
        ingestor = MQTTRecordingIngestor(file["bucket"], file["key"], processor)
        ingestors.append(ingestor)

# Start all ingestors
with ThreadPoolExecutor() as executor:
    wait([executor.submit(ingestor.start) for ingestor in ingestors])
