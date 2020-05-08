package com.dxc.analytics;

import java.time.Instant;
import java.util.Map;

public class HSLMessage {

    private String event;
    private String transportationMode;
    private Instant timestamp;

    public static HSLMessage fromMap(Map<String, Object> map) {
        var message = new HSLMessage();

        var root = map.entrySet().iterator().next();
        message.event = root.getKey().toLowerCase();

        var data = (Map<String, Object>) root.getValue();
        message.timestamp = Instant.parse((String) data.get("tst"));

        return message;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTransportationMode() {
        return transportationMode;
    }

    public void setTransportationMode(String transportationMode) {
        this.transportationMode = transportationMode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HSLMessage{" +
                "event='" + event + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
