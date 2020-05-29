"""Publish an order every second"""
import random
import time
import json
import uuid

import paho.mqtt.client as mqtt


if __name__ == "__main__":
    client = mqtt.Client()

    client.connect("localhost", 1883)
    client.loop_start()

    while True:
        client.publish(
            "carpool/order",
            json.dumps(
                {
                    "id": str(uuid.uuid4()),
                    "destination": f"destination-{random.randint(1, 100)}",
                    "name": "John Doe",
                }
            ),
            qos=1,
        )
        time.sleep(1)
