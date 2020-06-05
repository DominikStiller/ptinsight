import eventlet

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
    try:
        consumer = kafka.KafkaConsumer("egress.arrival-count", **config)
        for message in consumer:
            socketio.emit("arrival-count", {"count": int(message.value.decode())})
    except kafka.errors.NoBrokersAvailable:
        app.logger.error("Cannot connect to Kafka bootstrap servers")


if __name__ == "__main__":
    try:
        with open("config/ui.yaml") as f:
            config = yaml.safe_load(f)
    except FileNotFoundError:
        app.logger.error("Config file not found")
        sys.exit(1)

    threading.Thread(target=receive_from_kafka, args=(config["kafka"],)).start()

    socketio.run(app, host="0.0.0.0", port=8080)
