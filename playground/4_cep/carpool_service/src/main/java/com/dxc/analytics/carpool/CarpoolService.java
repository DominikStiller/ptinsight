package com.dxc.analytics.carpool;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CarpoolService {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        var orders = env.addSource(new OrderMQTTSource());
        orders.print();
        orders.addSink(new PickupMQTTSink());

        env.execute("Carpool Service");
    }
}
