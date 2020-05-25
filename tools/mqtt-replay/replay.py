#!/usr/bin/env python

import argparse
import base64
import os
import re
import sched
import threading
import time
from datetime import datetime

import paho.mqtt.client as mqtt


class Replayer:

    def __init__(self, host, port, recording_file):
        self.file = open(recording_file, 'r', encoding='utf-8')
        self.host = host
        self.port = port

        self.client = mqtt.Client()
        if port == 8883:
            self.client.tls_set()

        self.original_broker = self.file.readline()[8:-1]
        self.original_topics = self.file.readline()[8:-1]
        self.original_t_start = datetime.fromisoformat(self.file.readline()[12:-1]).replace(microsecond=0)
        print(f"Recording of {self.original_broker} at {str(self.original_t_start)}")
        print(f"Topics: {self.original_topics}\n")

    def start(self):
        self.client.connect(self.host, self.port, keepalive=60)

        message_regex = re.compile(r'(\S+) "(.+)" (\d) (\d) (\S*)')
        count = 0

        scheduler = sched.scheduler(time.perf_counter, time.sleep)
        # Custom run method is necessary to prevent scheduler from exiting early because no events are scheduled yet
        # Only returns when no events are in the queue but there were some earlier
        def run_scheduler():
            ran_once = False
            while True:
                event = scheduler.run(blocking=False)
                if event:
                    ran_once = True
                elif ran_once:
                    break
        thread = threading.Thread(target=run_scheduler)
        thread.start()
        t_start = time.perf_counter()

        self.file.readline()
        while True:
            line = self.file.readline()
            if not line:
                break
            count += 1

            # Parse record
            t_offset, topic, qos, retain, payload = message_regex.match(line).groups()
            t_offset = float(t_offset)
            qos = int(qos)
            retain = int(retain)
            payload = base64.b64decode(payload)

            # Publish via MQTT
            # Use factory method to force early binding for variables
            def make_publish(t_offset, topic, payload, qos, retain):
                def _publish():
                    print(f"(t={t_offset:.2f}) topic={topic} qos={qos} retain={retain}")
                    self.client.publish(topic, payload, qos, retain)
                return _publish
            scheduler.enterabs(t_start + t_offset, 1, make_publish(t_offset, topic, payload, qos, retain))

        thread.join()
        self.file.close()
        print(f"\nReplayed {count} messages from {os.path.realpath(self.file.name)}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Replay MQTT messages.")
    parser.add_argument('host', type=str)
    parser.add_argument('port', type=int)
    parser.add_argument('recording', type=str)

    args = parser.parse_args()

    rep = Replayer(args.host, args.port, args.recording)
    rep.start()