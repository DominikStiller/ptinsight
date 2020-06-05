package com.dxc.ptinsight.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.dxc.ptinsight.Event;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
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
           .keyBy(new KeySelector<Event, String>() {
               @Override
               public String getKey(Event value) throws Exception {
                   return (String) value.getPayload().getOrDefault("vt", "");
               }
           })
           .timeWindow(Time.seconds(1))
           .process(new ProcessWindowFunction<Event, Event, String, TimeWindow>() {
               @Override
               public void process(String key, Context context, Iterable<Event> elements, Collector<Event> out) throws Exception {
                   AtomicInteger count = new AtomicInteger();
                   elements.forEach((e -> count.getAndIncrement()));

                   var payload = new HashMap<String, Object>();
                   payload.put("count", count);
                   payload.put("vt", key);
                   out.collect(new Event(payload));
               }
           })
           .addSink(kafkaProducer);

        env.execute("PT Insight Processing");
    }
}
