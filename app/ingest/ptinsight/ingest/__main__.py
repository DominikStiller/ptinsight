import logging
import sys
from concurrent.futures import wait
from concurrent.futures.thread import ThreadPoolExecutor

import kafka.errors
import yaml

from ptinsight.ingest.ingestor import MQTTIngestor, Ingestor

logger = logging.getLogger(__name__)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    try:
        with open("config/ingest.yaml") as f:
            config = yaml.safe_load(f)
    except FileNotFoundError:
        logger.error("Config file not found")
        sys.exit(1)

    try:
        Ingestor.create_producer(config["kafka"])
    except kafka.errors.NoBrokersAvailable:
        logger.error("Cannot connect to Kafka bootstrap servers")
        sys.exit(1)

    ingestors = []
    for source in config["sources"]:
        if source["type"] == "mqtt":
            broker = source["broker"]
            ingestor = MQTTIngestor(
                broker["host"], int(broker["port"]), source["streams"]
            )
            ingestors.append(ingestor)

    with ThreadPoolExecutor() as executor:
        wait([executor.submit(ingestor.start) for ingestor in ingestors])
