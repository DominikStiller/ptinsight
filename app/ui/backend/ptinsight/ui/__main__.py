import eventlet

eventlet.monkey_patch()

from ptinsight.ui.server import Server


import os
import sys
import logging
import yaml

logger = logging.getLogger(__name__)


if __name__ == "__main__":
    if os.path.exists("config/ui.yaml"):
        config_path = "config/ui.yaml"
    elif os.path.exists("config/ui.default.yaml"):
        config_path = "config/ui.default.yaml"
    else:
        print("Config file not found")
        sys.exit(1)

    with open(config_path) as f:
        config = yaml.safe_load(f)

    logging.basicConfig()
    logging.getLogger("ptinsight").setLevel(config["logging"]["level"].upper())

    Server(config).start()
