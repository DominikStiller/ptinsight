package com.dxc.analytics.carpool;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
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

        orders.keyBy((KeySelector<OrderMessage, String>) OrderMessage::getDestination)
              .process(new CarpoolMatcher(Time.seconds(5), 3))
              .addSink(new PickupMQTTSink());

        env.execute("Carpool Service");
    }
}

/**
 * Matches riders with the same destination
 * Match is complete when maxRiders are ready or maxWaitTime for the first rider has passed
 */
class CarpoolMatcher extends KeyedProcessFunction<String, OrderMessage, PickupMessage> {

    private transient ListState<PickupMessage.Order> orders;
    private transient ValueState<Long> timeout;

    private final Time maxWaitTime;
    private final int maxRiders;

    public CarpoolMatcher(Time maxWaitTime, int maxRiders) {
        this.maxWaitTime = maxWaitTime;
        this.maxRiders = maxRiders;
    }

    @Override
    public void open(Configuration parameters) {
        var ordersDescriptor =
                new ListStateDescriptor<PickupMessage.Order>("orders",
                                                             TypeInformation.of(new TypeHint<PickupMessage.Order>() {}));
        orders = getRuntimeContext().getListState(ordersDescriptor);

        var timeoutDescriptor = new ValueStateDescriptor<>("timeout", Types.LONG);
        timeout = getRuntimeContext().getState(timeoutDescriptor);
    }

    @Override
    public void processElement(OrderMessage value, Context ctx, Collector<PickupMessage> out) throws Exception {
        orders.add(new PickupMessage.Order(value.getId(), value.getName()));

        var count = countOrders();

        if (count == 1) {
            // Set timeout when first order for destination arrives
            long nextTimeout = ctx.timerService().currentProcessingTime() + this.maxWaitTime.toMilliseconds();
            ctx.timerService().registerProcessingTimeTimer(nextTimeout);
            timeout.update(nextTimeout);
        } else if (count == this.maxRiders) {
            pickupRiders(ctx, out);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<PickupMessage> out) throws Exception {
        pickupRiders(ctx, out);
    }

    private int countOrders() throws Exception {
        var count = new AtomicInteger();
        orders.get().forEach(e -> count.getAndIncrement());
        return count.intValue();
    }

    private void pickupRiders(Context ctx, Collector<PickupMessage> out) throws Exception {
        // Aggregate orders and send pickup message
        var ordersList = new ArrayList<PickupMessage.Order>(countOrders());
        orders.get().forEach(ordersList::add);
        out.collect(new PickupMessage(ctx.getCurrentKey(), ordersList));

        // Reset orders and timeout
        orders.clear();
        ctx.timerService().deleteProcessingTimeTimer(timeout.value());
        timeout.clear();
    }
}
