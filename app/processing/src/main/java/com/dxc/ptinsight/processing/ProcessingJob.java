package com.dxc.ptinsight.processing;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.dxc.ptinsight.proto.Base;
import com.dxc.ptinsight.proto.egress.Counts;
import com.dxc.ptinsight.proto.ingress.HslRealtime;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import static com.dxc.ptinsight.proto.Base.Event;

public class ProcessingJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO switch to event time
        // TODO get bootstrap servers from config
        var kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "localhost:9092");

        var kafkaSource = new FlinkKafkaConsumer<>(
                "ingress.arrival",
                new EventDeserializationSchema(),
                kafkaProps
        );

        var kafkaProducer = new FlinkKafkaProducer<>(
                "egress.arrival-count",
                new EventSerializationSchema("egress.arrival-count"),
                kafkaProps,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE
        );

        env.addSource(kafkaSource)
           .map(new MapFunction<Event, HslRealtime.Arrival>() {
               @Override
               public HslRealtime.Arrival map(Event value) throws Exception {
                   return value.getDetails().unpack(HslRealtime.Arrival.class);
               }
           })
           .keyBy(new KeySelector<HslRealtime.Arrival, Base.VehicleType>() {
               @Override
               public Base.VehicleType getKey(HslRealtime.Arrival value) throws Exception {
                   return value.getVehicle().getType();
               }
           })
           .timeWindow(Time.seconds(1))
           .process(new ProcessWindowFunction<HslRealtime.Arrival, Event, Base.VehicleType, TimeWindow>() {
               @Override
               public void process(Base.VehicleType key, Context context, Iterable<HslRealtime.Arrival> elements, Collector<Event> out) {
                   AtomicInteger count = new AtomicInteger();
                   elements.forEach((e -> count.getAndIncrement()));

                   var details = Counts.ArrivalCount.newBuilder()
                                                    .setVehicleType(key)
                                                    .setCount(count.intValue())
                                                    .setWindowStart(Timestamp.newBuilder()
                                                                             .setSeconds(context.window().getStart() / 1000)
                                                                             .build())
                                                    .setWindowEnd(Timestamp.newBuilder()
                                                                           .setSeconds(context.window().getEnd() / 1000)
                                                                           .build())
                                                    .build();
                   var event = Event.newBuilder()
                                    .setEventTimestamp(Timestamp.newBuilder().setSeconds(context.window().getStart() / 1000)
                                                                .build())
                                    .setIngestionTimestamp(Timestamp.newBuilder()
                                                                    .setSeconds(Instant.now().getEpochSecond()).build())
                                    .setDetails(Any.pack(details))
                                    .build();
                   out.collect(event);
               }
           })
           .addSink(kafkaProducer);

        env.execute("PT Insight Processing");
    }
}
