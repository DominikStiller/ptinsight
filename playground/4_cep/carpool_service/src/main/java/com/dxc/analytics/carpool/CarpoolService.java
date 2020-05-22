package com.dxc.analytics.carpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class CarpoolService {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        var orders = env.addSource(new OrderMQTTSource());
        orders.print();

        orders
            .keyBy(new KeySelector<OrderMessage, String>() {
                @Override
                public String getKey(OrderMessage value) throws Exception {
                    return value.getDestination();
                }
            })
            .process(new KeyedProcessFunction<String, OrderMessage, PickupMessage>() {

                private transient ListState<PickupMessage.Order> orders;

                @Override
                public void open(Configuration parameters) throws Exception {
                    var descriptor = new ListStateDescriptor<PickupMessage.Order>("orders",
                                                                                  TypeInformation.of(new TypeHint<PickupMessage.Order>() {}));
                    orders = getRuntimeContext().getListState(descriptor);
                }

                @Override
                public void processElement(OrderMessage value, Context ctx, Collector<PickupMessage> out) throws Exception {
                    orders.add(new PickupMessage.Order(value.getId(), value.getName()));

                    var count = new AtomicInteger();
                    orders.get().forEach(e -> count.getAndIncrement());

                    if (count.intValue() == 3) {
                        var ordersList = new ArrayList<PickupMessage.Order>(count.intValue());
                        orders.get().forEach(ordersList::add);
                        out.collect(new PickupMessage(value.getDestination(), ordersList));
                        orders.clear();
                    }
                }
            })
            .addSink(new PickupMQTTSink());

        env.execute("Carpool Service");
    }
}
