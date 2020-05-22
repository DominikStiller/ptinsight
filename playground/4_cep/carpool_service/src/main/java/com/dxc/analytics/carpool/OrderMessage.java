package com.dxc.analytics.carpool;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderMessage {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("name")
    private String customerName;
    @JsonProperty("destination")
    private String destination;

    @Override
    public String toString() {
        return "OrderMessage{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
