// Adapted from https://github.com/luckyyuyong/flink-mqtt-connector/blob/master/src/main/java/io/github/pkhanal/sources/MQTTSource.java
package com.dxc.analytics.carpool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class OrderMQTTSource extends RichSourceFunction<OrderMessage> {

    private transient MqttClient client;
    private transient ObjectMapper mapper;
    private transient volatile boolean running;
    private transient Object waitLock;

    @Override
    public void run(final SourceContext<OrderMessage> ctx) throws Exception {
        var connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);

        client = new MqttClient("tcp://localhost:1883", "carpool-service-order");
        client.connect(connectOptions);

        mapper = new ObjectMapper();

        client.subscribe("carpool/order", (topic, data) -> {
            var json = new String(data.getPayload(), StandardCharsets.UTF_8);
            ctx.collect(mapper.readValue(json, OrderMessage.class));
        });

        running = true;
        waitLock = new Object();

        while (running) {
            synchronized (waitLock) {
                waitLock.wait(100L);
            }
        }
    }

    @Override
    public void cancel() {
        close();
    }

    public void close() {
        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (MqttException exception) {

        } finally {
            this.running = false;
        }

        // leave main method
        synchronized (waitLock) {
            waitLock.notify();
        }
    }
}
