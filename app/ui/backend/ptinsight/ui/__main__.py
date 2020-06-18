import eventlet

eventlet.monkey_patch()

from ptinsight.ui.server import Server


import os
import sys
import logging
import yaml

logger = logging.getLogger(__name__)


if __name__ == "__main__":
    logging.basicConfig()
    logging.getLogger("ptinsight").setLevel(logging.INFO)

    if os.path.exists("config/ui.yaml"):
        config_path = "config/ui.yaml"
    elif os.path.exists("config/ui.default.yaml"):
        config_path = "config/ui.default.yaml"
    else:
        logger.error("Config file not found")
        sys.exit(1)

    with open(config_path) as f:
        config = yaml.safe_load(f)
    Server(config).start()
