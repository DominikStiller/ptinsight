package com.dxc.analytics.carpool;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PickupMessage {

    @JsonProperty("destination")
    private String destination;
    @JsonProperty("orders")
    private List<Order> orders;

    public PickupMessage(String destination, List<Order> orders) {
        this.destination = destination;
        this.orders = orders;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    static class Order {
        @JsonProperty("id")
        private UUID id;
        @JsonProperty("name")
        private String name;

        public Order(UUID id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
