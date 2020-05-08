package com.dxc.analytics;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicTransportAnalysis {

    private static Logger log = LoggerFactory.getLogger(PublicTransportAnalysis.class);

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        var windowSize = 5;

        var stream = env.addSource(new HSLMQTTSource(List.of("arr", "dep"), List.of("bus", "tram")));
        var count = stream
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<>(Time.seconds(2)) {
                    @Override
                    public long extractTimestamp(HSLMessage element) {
                        return element.getTimestamp().toEpochMilli();
                    }
                })
                .keyBy(new KeySelector<HSLMessage, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> getKey(HSLMessage value) {
                        return Tuple2.of(value.getEvent(), value.getTransportationMode());
                    }
                })
                .timeWindow(Time.seconds(windowSize))
                .process(new ProcessWindowFunction<HSLMessage, Tuple4<String, String, Integer, Long>, Tuple2<String, String>, TimeWindow>() {
                    @Override
                    public void process(Tuple2<String, String> key, Context context, Iterable<HSLMessage> elements, Collector<Tuple4<String, String, Integer, Long>> out) {
                        // Count arrivals/departures and add time stamp
                        AtomicInteger count = new AtomicInteger();
                        elements.forEach((e -> count.getAndIncrement()));

                        var message = elements.iterator().next();
                        out.collect(Tuple4.of(message.getEvent(), message.getTransportationMode(), count.intValue(), context.currentWatermark()));
                    }
                })
                .map(new MapFunction<Tuple4<String, String, Integer, Long>, String>() {
                    @Override
                    public String map(Tuple4<String, String, Integer, Long> value) {
                        var windowEnd = Instant.ofEpochMilli(value.f3);
                        var windowStart = windowEnd.minus(windowSize, ChronoUnit.SECONDS);

                        var dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                                                             .withZone(ZoneId.of("Europe/Berlin"));

                        return String.format("%d %s %s between %s and %s",
                                             value.f2,
                                             value.f1,
                                             value.f0.equals("dep") ? "departures" : "arrivals",
                                             dateFormatter.format(windowStart),
                                             dateFormatter.format(windowEnd));
                    }
                });
        count.print();

        env.execute("Public Transport Analysis");
    }
}
