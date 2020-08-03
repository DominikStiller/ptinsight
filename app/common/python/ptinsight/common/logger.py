import logging


def setup_logger(ptinsight_level: str):
    logging.basicConfig()
    logging.getLogger("ptinsight").setLevel(ptinsight_level.upper())
