package com.dxc.ptinsight;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {

    @JsonProperty("event_ts")
    private Instant eventTimestamp;
    @JsonProperty("payload")
    private Map<String, Object> payload;

    public Event() {
    }

    public Event(Map<String, Object> payload) {
        this(Instant.now(), payload);
    }

    public Event(Instant eventTimestamp, Map<String, Object> payload) {
        this.eventTimestamp = eventTimestamp;
        this.payload = payload;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
