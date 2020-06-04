package com.dxc.ptinsight;

import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

public class ProcessingJob {
    
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO get bootstrap servers from config
        // TODO implement event JSON parsing
        var kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "localhost:9092");

        var kafkaSource = new FlinkKafkaConsumer<>(
                "ingress.arrival",
                new SimpleStringSchema(),
                kafkaProps
        );

        var kafkaProducer = new FlinkKafkaProducer<>(
                "egress.arrival-count",
                new ProducerStringSerializationSchema("egress.arrival-count"),
                kafkaProps,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE
        );

        env.addSource(kafkaSource)
           .map(new MapFunction<String, Integer>() {
               @Override
               public Integer map(String value) {
                   return 1;
               }
           })
           .timeWindowAll(Time.seconds(1))
           .reduce(new ReduceFunction<Integer>() {
               @Override
               public Integer reduce(Integer value1, Integer value2) {
                   return value1 + value2;
               }
           })
           .map(new MapFunction<Integer, String>() {
               @Override
               public String map(Integer value) throws Exception {
                   return value.toString();
               }
           })
           .addSink(kafkaProducer);

        env.execute("PT Insight Processing");
    }
}
