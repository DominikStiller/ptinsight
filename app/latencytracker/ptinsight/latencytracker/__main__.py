import logging
import os
import sys
import yaml

from ptinsight.latencytracker.tracker import LatencyTracker

logger = logging.getLogger(__name__)


# Load config
if os.path.exists("config/latencytracker.yaml"):
    config_path = "config/latencytracker.yaml"
elif os.path.exists("config/latencytracker.default.yaml"):
    config_path = "config/latencytracker.default.yaml"
else:
    print("Config file not found")
    sys.exit(1)

with open(config_path) as f:
    config = yaml.safe_load(f)

logging.basicConfig()
logging.getLogger("ptinsight").setLevel(config["logging"]["level"].upper())

LatencyTracker(config).start()
