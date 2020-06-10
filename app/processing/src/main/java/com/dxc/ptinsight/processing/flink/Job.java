package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.proto.Base.Event;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.time.Instant;
import java.util.Properties;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

public abstract class Job {

  private final String name;
  private final StreamExecutionEnvironment env;
  private static final Properties props = new Properties();

  public Job(String name) {
    this.name = name;

    env = StreamExecutionEnvironment.getExecutionEnvironment();
    configureEnvironment();
    configureKafka();
  }

  private void configureEnvironment() {
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
  }

  private void configureKafka() {
    props.clear();
    props.setProperty("bootstrap.servers", "localhost:9092");
  }

  protected final <T extends Message> SingleOutputStreamOperator<T> source(
      String topic, Class<T> clazz) {
    var consumer = new FlinkKafkaConsumer<>(topic, new EventDeserializationSchema(), Job.props);
    return env.addSource(consumer)
        .assignTimestampsAndWatermarks(
            new BoundedOutOfOrdernessTimestampExtractor<>(Time.seconds(1)) {
              @Override
              public long extractTimestamp(Event element) {
                return Timestamps.toInstant(element.getEventTimestamp()).toEpochMilli();
              }
            })
        .map(new ExtractDetailMapFunction<T>(clazz))
        .returns(clazz);
  }

  protected final SinkFunction<Event> sink(String topic) {
    return new FlinkKafkaProducer<>(
        topic,
        new EventSerializationSchema(topic),
        props,
        FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
  }

  protected static Event output(Message details) {
    return output(details, null);
  }

  protected static Event output(Message details, Instant eventTimestamp) {
    var currentTimestamp = Timestamps.fromInstant(Instant.now());

    return Event.newBuilder()
        .setEventTimestamp(
            eventTimestamp != null ? Timestamps.fromInstant(eventTimestamp) : currentTimestamp)
        .setIngestionTimestamp(currentTimestamp)
        .setDetails(Any.pack(details))
        .build();
  }

  public final void execute() throws Exception {
    setup();
    env.execute(this.name);
  }

  protected abstract void setup();
}
