import eventlet

eventlet.monkey_patch()

import os
import sys
import threading

import h3.api.basic_int as h3
import kafka
import kafka.errors
from kafka import KafkaConsumer
import yaml
from flask import Flask
from flask_socketio import SocketIO
from ptinsight.common.proto.egress.counts_pb2 import VehicleCount
from ptinsight.common.serialize import deserialize

app = Flask(__name__, static_folder="../../../frontend/dist", static_url_path="/")
socketio = SocketIO(app)


@app.route("/")
def page():
    return app.send_static_file("index.html")


def receive_from_kafka(config: dict):
    if "protobuf_format" in config:
        protobuf_format = config.pop("protobuf_format")
    else:
        protobuf_format = "json"
    try:
        consumer = KafkaConsumer("egress.vehicle-count", **config)
        for message in consumer:
            event = deserialize(message.value, protobuf_format)
            vehicle_count = VehicleCount()
            event.details.Unpack(vehicle_count)

            socketio.emit(
                "vehicle-count",
                {
                    "geocell": h3.h3_to_string(vehicle_count.geocell),
                    "count": vehicle_count.count,
                },
            )
    except kafka.errors.NoBrokersAvailable:
        app.logger.error("Cannot connect to Kafka bootstrap servers")


if __name__ == "__main__":
    if os.path.exists("config/ui.yaml"):
        config_path = "config/ui.yaml"
    elif os.path.exists("config/ui.default.yaml"):
        config_path = "config/ui.default.yaml"
    else:
        app.logger.error("Config file not found")
        sys.exit(1)

    with open(config_path) as f:
        config = yaml.safe_load(f)

    threading.Thread(target=receive_from_kafka, args=(config["kafka"],)).start()

    socketio.run(app, host="0.0.0.0", port=8080)
