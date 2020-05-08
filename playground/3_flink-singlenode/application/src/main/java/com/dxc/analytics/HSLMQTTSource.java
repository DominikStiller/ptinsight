// Adapted from https://github.com/luckyyuyong/flink-mqtt-connector/blob/master/src/main/java/io/github/pkhanal/sources/MQTTSource.java
package com.dxc.analytics;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

/**
 * Connector for Real-Time API of Helsinki Transport Agency
 * https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions
 */
public class HSLMQTTSource extends RichSourceFunction<HSLMessage> {

    private final Collection<String> events;
    private final Collection<String> transportationModes;

    private transient MqttClient client;
    private transient ObjectMapper mapper;
    private transient volatile boolean running;
    private transient Object waitLock;

    public HSLMQTTSource(Collection<String> events, Collection<String> transportationModes) {
        this.events = events;
        this.transportationModes = transportationModes;
    }

    @Override
    public void run(final SourceContext<HSLMessage> ctx) throws Exception {
        var connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);

        client = new MqttClient("ssl://mqtt.hsl.fi:8883", "eda-demo");
        client.connect(connectOptions);

        mapper = new ObjectMapper();
        var typeRef = new TypeReference<HashMap<String, Object>>() {};

        for(String event: this.events) {
            for (String transportationMode: this.transportationModes) {
                var topic = String.format("/hfp/v2/journey/ongoing/%s/%s/+/+/+/+/+/+/+/+/#", event, transportationMode);
                client.subscribe(topic, (topics, data) -> {
                    var json = new String(data.getPayload(), StandardCharsets.UTF_8);
                    var message = HSLMessage.fromMap(mapper.readValue(json, typeRef));
                    message.setTransportationMode(transportationMode);
                    ctx.collectWithTimestamp(message, message.getTimestamp().toEpochMilli());
                });
            }
        }

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
