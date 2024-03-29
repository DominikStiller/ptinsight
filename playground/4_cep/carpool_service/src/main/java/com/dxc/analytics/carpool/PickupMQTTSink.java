package com.dxc.analytics.carpool;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PickupMQTTSink extends RichSinkFunction<PickupMessage> {

    private transient MqttClient client;
    private transient ObjectMapper mapper;

    @Override
    public void invoke(PickupMessage value, Context context) throws Exception {
        var message = new MqttMessage(mapper.writeValueAsBytes(value));
        client.publish("carpool/pickup", message);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        var connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);

        client = new MqttClient("tcp://localhost:1883",
                                "carpool-service-pickup-" + UUID.randomUUID().toString(),
                                new MemoryPersistence());
        client.connect(connectOptions);

        mapper = new ObjectMapper();
    }

    public void close() {
        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (MqttException exception) {
        }
    }
}
