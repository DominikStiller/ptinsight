import eventlet
from ptinsight.common import VehicleType
from ptinsight.common.proto.egress.counts_pb2 import ArrivalCount
from ptinsight.common.serialize import deserialize

eventlet.monkey_patch()

import sys
import threading

import kafka
import kafka.errors
import yaml
from flask import Flask
from flask_socketio import SocketIO
from sassutils.wsgi import SassMiddleware

app = Flask(__name__, template_folder="../../templates", static_folder="../../static")
app.wsgi_app = SassMiddleware(
    app.wsgi_app,
    {"ptinsight.ui": ("../../static/scss", "../../static/css", "/static/css", True)},
)
socketio = SocketIO(app)


@app.route("/")
def page():
    return app.send_static_file("html/index.html")


def receive_from_kafka(config: dict):
    if "protobuf_format" in config:
        protobuf_format = config.pop("protobuf_format")
    else:
        protobuf_format = "json"
    try:
        consumer = kafka.KafkaConsumer("egress.arrival-count", **config)
        for message in consumer:
            event = deserialize(message.value, protobuf_format)
            arrival_count = ArrivalCount()
            event.details.Unpack(arrival_count)

            socketio.emit("arrival-count", {
                "ts": arrival_count.window_start.ToJsonString(),
                "vt": VehicleType.Name(arrival_count.vehicle_type).lower(),
                "count": arrival_count.count
            })
    except kafka.errors.NoBrokersAvailable:
        app.logger.error("Cannot connect to Kafka bootstrap servers")


if __name__ == "__main__":
    if os.path.exists("config/ui.yaml"):
        config_path = "config/ui.yaml"
    elif os.path.exists("config/ui.default.yaml"):
        config_path = "config/ui.default.yaml"
    else:
        logger.error("Config file not found")
        sys.exit(1)

    with open(config_path) as f:
        config = yaml.safe_load(f)

    threading.Thread(target=receive_from_kafka, args=(config["kafka"],)).start()

    socketio.run(app, host="0.0.0.0", port=8080)
