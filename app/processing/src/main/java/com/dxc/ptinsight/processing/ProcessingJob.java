package com.dxc.ptinsight.processing;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.proto.Base.VehicleType;
import com.dxc.ptinsight.proto.egress.Counts.ArrivalCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.Arrival;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

public class ProcessingJob {

  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // TODO switch to event time
    // TODO get bootstrap servers from config
    var kafkaProps = new Properties();
    kafkaProps.setProperty("bootstrap.servers", "localhost:9092");

    var kafkaSource =
        new FlinkKafkaConsumer<>("ingress.arrival", new EventDeserializationSchema(), kafkaProps);

    var kafkaProducer =
        new FlinkKafkaProducer<>(
            "egress.arrival-count",
            new EventSerializationSchema("egress.arrival-count"),
            kafkaProps,
            FlinkKafkaProducer.Semantic.EXACTLY_ONCE);

    env.addSource(kafkaSource)
        .map(new ExtractDetailMapFunction<Arrival>(Arrival.class))
        .returns(Arrival.class)
        .keyBy(
            new KeySelector<Arrival, VehicleType>() {
              @Override
              public VehicleType getKey(Arrival value) throws Exception {
                return value.getVehicle().getType();
              }
            })
        .timeWindow(Time.seconds(1))
        .process(
            new ProcessWindowFunction<Arrival, Event, VehicleType, TimeWindow>() {
              @Override
              public void process(
                  VehicleType key,
                  Context context,
                  Iterable<Arrival> elements,
                  Collector<Event> out) {
                AtomicInteger count = new AtomicInteger();
                elements.forEach((e -> count.getAndIncrement()));

                var details =
                    ArrivalCount.newBuilder()
                        .setVehicleType(key)
                        .setCount(count.intValue())
                        .setWindowStart(
                            Timestamp.newBuilder()
                                .setSeconds(context.window().getStart() / 1000)
                                .build())
                        .setWindowEnd(
                            Timestamp.newBuilder()
                                .setSeconds(context.window().getEnd() / 1000)
                                .build())
                        .build();
                var event =
                    Event.newBuilder()
                        .setEventTimestamp(
                            Timestamp.newBuilder()
                                .setSeconds(context.window().getStart() / 1000)
                                .build())
                        .setIngestionTimestamp(
                            Timestamp.newBuilder()
                                .setSeconds(Instant.now().getEpochSecond())
                                .build())
                        .setDetails(Any.pack(details))
                        .build();
                out.collect(event);
              }
            })
        .addSink(kafkaProducer);

    env.execute("PT Insight Processing");
  }
}
