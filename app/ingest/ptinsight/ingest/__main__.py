import logging
import os
import sys
from concurrent.futures import wait
from concurrent.futures.thread import ThreadPoolExecutor

import kafka.errors
import yaml

from ptinsight.ingest.ingestor import MQTTIngestor, Ingestor
from ptinsight.ingest.processors import MQTTProcessor

logger = logging.getLogger(__name__)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    try:
        with open("config/ingest.yaml") as f:
            config = yaml.safe_load(f)
    except FileNotFoundError:
        logger.error("Config file not found")
        sys.exit(1)

    if "INGEST_DEBUG" in os.environ:
        Ingestor.create_debug_producer(config["kafka"])
    else:
        try:
            Ingestor.create_kafka_producer(config["kafka"])
        except kafka.errors.NoBrokersAvailable:
            logger.error("Cannot connect to Kafka bootstrap servers")
            sys.exit(1)

    processors = {}
    for type in [MQTTProcessor]:
        classes = type.__subclasses__()
        for cls in classes:
            processors[cls.name()] = cls

    ingestors = []
    for source in config["sources"]:
        if source["type"] == "mqtt":
            broker = source["broker"]
            processor = processors[source["processor"]](source["config"])
            ingestor = MQTTIngestor(broker["host"], int(broker["port"]), processor)
            ingestors.append(ingestor)

    with ThreadPoolExecutor() as executor:
        wait([executor.submit(ingestor.start) for ingestor in ingestors])
