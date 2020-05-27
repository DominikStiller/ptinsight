#!/usr/bin/env python

import argparse
import base64
import logging
import os
import time
from datetime import datetime, timezone

import paho.mqtt.client as mqtt


logger = logging.getLogger('mqtt-recorder')
logging.basicConfig(level=logging.INFO, format='%(message)s')


class Recorder:

    def __init__(self, host, port, topics):
        self.host = host
        self.port = port
        self.topics = topics
        self.count = 0
        self.t_start = None

        self.client = mqtt.Client()
        self.client.on_connect = self._mqtt_on_connect
        self.client.on_message = self._mqtt_on_message
        if port == 8883:
            self.client.tls_set()

        rec_id = datetime.now().replace(microsecond=0).isoformat().replace(":", "-")
        os.makedirs(f'recordings/{host}', exist_ok=True)
        self.file = open(f'recordings/{host}/{rec_id}.rec', 'w', encoding='utf-8')

        self.file.write(f"Broker: {host}:{port}\n")
        self.file.write(f"Topics: {','.join(topics)}\n")

    def start(self):
        self.client.connect(self.host, self.port, keepalive=60)

        self.t_start = time.perf_counter()
        t_start_absolute = datetime.now(timezone.utc)
        self.file.write(f"Start time: {t_start_absolute.isoformat()}\n\n")

        logger.info(f"Recording of {self.host}:{self.port} at {t_start_absolute}")
        logger.info(f"Topics: {','.join(self.topics)}\n")

        self.client.loop_start()

    def stop(self):
        self.client.disconnect()
        self.file.close()
        logger.info(f"\nRecorded {self.count} messages to {os.path.realpath(self.file.name)}")

    def _mqtt_on_connect(self, client, userdata, flags, rc):
        for topic in self.topics:
            client.subscribe(topic, qos=1)

    def _mqtt_on_message(self, client, userdata, msg: mqtt.MQTTMessage):
        self.count += 1

        t_offset = time.perf_counter() - self.t_start

        self.file.write(f'{t_offset} "{msg.topic}" {msg.qos} {msg.retain} ')
        self.file.write(base64.b64encode(msg.payload).decode())
        self.file.write('\n')
        logger.debug(f"(t={t_offset:.2f}) topic={msg.topic} qos={msg.qos} retain={msg.retain}")


def convert_to_seconds(duration):
    seconds_per_unit = {"s": 1, "m": 60, "h": 3600, "d": 86400, "w": 604800}
    return float(duration[:-1]) * seconds_per_unit[duration[-1]]


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Record MQTT messages.")
    parser.add_argument('host', type=str)
    parser.add_argument('port', type=int)
    parser.add_argument('duration', type=str)
    parser.add_argument('topics', type=str, nargs='+')
    parser.add_argument('-v', action='store_true')

    args = parser.parse_args()

    if args.v:
        logger.setLevel(logging.DEBUG)

    rec = Recorder(args.host, args.port, args.topics)
    rec.start()

    t_end = time.monotonic() + convert_to_seconds(args.duration)
    while time.monotonic() < t_end:
        time.sleep(1)

    rec.stop()
