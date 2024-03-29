"""Drop-in replacement for Flink CEP to answer orders with random delay"""
import concurrent.futures
import json
import random
import time

import paho.mqtt.client as mqtt

executor = concurrent.futures.ThreadPoolExecutor()


def on_connect(client, userdata, flags, rc):
    client.subscribe("carpool/order", qos=1)


def on_message(client, userdata, msg):
    if msg.topic == "carpool/order":
        executor.submit(handle_order, client, msg)


def handle_order(client, msg):
    order = json.loads(msg.payload)
    time.sleep(random.randint(1, 3))
    client.publish(
        "carpool/pickup",
        json.dumps(
            {
                "destination": order["destination"],
                "orders": [{"id": order["id"], "name": order["name"]}],
            }
        ),
        qos=1,
    )


if __name__ == "__main__":
    client = mqtt.Client()

    client.on_connect = on_connect
    client.on_message = on_message

    client.connect("localhost", 1883)
    client.loop_forever()
