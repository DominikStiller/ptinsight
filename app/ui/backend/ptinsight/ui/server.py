import threading

from flask import Flask
from flask_socketio import SocketIO

from ptinsight.ui.transport import KafkaToSocketioBridge


class Server:
    def __init__(self, config: dict):
        self.app = Flask(
            __name__, static_folder="../../../frontend/dist", static_url_path="/"
        )
        self.socketio = SocketIO(self.app)
        self.bridge = KafkaToSocketioBridge(self.socketio, config["kafka"])

        self.app.add_url_rule(
            "/", view_func=lambda: self.app.send_static_file("index.html")
        )

    def start(self):
        threading.Thread(target=self.bridge.start).start()
        self.socketio.run(self.app, host="0.0.0.0", port=8080)
