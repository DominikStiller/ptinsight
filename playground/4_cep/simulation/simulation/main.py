"""
Simulates a city with 10 citizens moving between 5 locations by using a carpool service
Citizens order a car and only move once it arrives
The carpool service accepts orders and dispatches cars using MQTT
"""
import enum
import json
import random
import uuid

import simpy
import paho.mqtt.client as mqtt


class Location(enum.Enum):
    RESTAURANT = enum.auto()
    BAR = enum.auto()
    BEACH = enum.auto()
    CITY_HALL = enum.auto()
    HOTEL = enum.auto()

    @staticmethod
    def random(current=None):
        choice = random.choice(list(Location))
        while current is not None and choice == current:
            # Prevent people from remaining at the same location
            choice = random.choice(list(Location))
        return choice

    def __str__(self):
        return self.name


class CarpoolService:
    def __init__(self, env: simpy.Environment):
        self.env = env
        self.orders = {}

        self.client = mqtt.Client()
        self.client.on_connect = self._on_mqtt_connect
        self.client.on_message = self._on_mqtt_message
        self.client.connect("localhost", 1883)
        self.client.loop_start()

    def _on_mqtt_connect(self, client, userdata, flags, rc):
        self.client.subscribe('carpool/pickup', qos=1)

    def _on_mqtt_message(self, client, userdata, msg):
        payload = json.loads(msg.payload)
        if msg.topic == 'carpool/pickup':
            destination = payload["destination"]
            rider_names = [order["name"] for order in payload["orders"]]
            print(f'(t={env.now}) PICKUP: {", ".join(rider_names)} to {destination}')
            for order in payload['orders']:
                self.orders[order['id']].succeed(destination)
                del self.orders[order['id']]

    def order(self, name, destination: Location):
        id = str(uuid.uuid4())
        self.client.publish("carpool/order", json.dumps({
            'id': str(id),
            'destination': str(destination),
            'name': name
        }), qos=1)
        print(f'(t={env.now}) ORDER: {name} to {destination}')

        self.orders[id] = self.env.event()
        return self.orders[id]


class Citizen:
    def __init__(self, name: str, env: simpy.Environment, carpool_service: CarpoolService):
        self.env = env
        self.carpool_service = carpool_service
        self.action = env.process(self.run())

        self.name = name
        self.location = Location.random()

    def run(self):
        while True:
            # Remain at the location for a random time
            yield self.env.timeout(random.randint(2, 7))

            destination = yield self.carpool_service.order(self.name, Location.random(self.location))
            print(f'(t={env.now}) -> {self.name} went from {self.location} to {destination}')
            self.location = destination


def clock(env: simpy.Environment):
    # Required to keep simulation going
    while True:
        yield env.timeout(1)


if __name__ == '__main__':
    citizen_names = [
        'Anna', 'Brad', 'Clara', 'Dave', 'Emilia',
        'Freddy', 'Gwen', 'Harry', 'Isabell', 'Jacob'
    ]

    env = simpy.rt.RealtimeEnvironment(strict=False)
    carpool_service = CarpoolService(env)
    citizens = [Citizen(name, env, carpool_service) for name in citizen_names[:10]]
    env.process(clock(env))
    env.run(until=60)
