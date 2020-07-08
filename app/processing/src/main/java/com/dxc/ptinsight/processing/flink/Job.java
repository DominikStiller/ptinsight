package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.proto.Base.Event;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public abstract class Job {

  private final String name;
  private final StreamExecutionEnvironment env;
  private StreamTableEnvironment tableEnv;
  private static final Properties kafkaProps = new Properties();

  public Job(String name) {
    this(name, true, 10000);
  }

  public Job(String name, boolean withCheckpointing, int checkpointingInterval) {
    this.name = name;

    env = StreamExecutionEnvironment.getExecutionEnvironment();
    configureEnvironment(withCheckpointing, checkpointingInterval);
    configureKafka();
  }

  private void configureEnvironment(boolean withCheckpointing, int checkpointingInterval) {
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.setParallelism(2);

    if (withCheckpointing) {
      env.enableCheckpointing(checkpointingInterval, CheckpointingMode.EXACTLY_ONCE);
      env.getCheckpointConfig().setMinPauseBetweenCheckpoints(checkpointingInterval / 2);
    }
  }

  private void configureKafka() {
    kafkaProps.clear();

    var kafkaConfig = EntryPoint.getConfiguration().kafka;
    kafkaProps.setProperty("bootstrap.servers", String.join(",", kafkaConfig.bootstrapServers));
    kafkaProps.setProperty("group.id", "ptinsight_" + this.name.replace(' ', '_'));
  }

  protected StreamTableEnvironment getTableEnvironment() {
    if (tableEnv == null) {
      var settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
      tableEnv = StreamTableEnvironment.create(env, settings);
    }
    return tableEnv;
  }

  protected final <T extends Message> SingleOutputStreamOperator<T> source(
      String topic, Class<T> clazz) {
    var consumer =
        new FlinkKafkaConsumer<>(topic, new EventDeserializationSchema(), Job.kafkaProps);
    consumer.setStartFromLatest();
    consumer.assignTimestampsAndWatermarks(
        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(1))
            .withTimestampAssigner(
                (element, recordTimestamp) ->
                    Timestamps.toInstant(element.getEventTimestamp()).toEpochMilli()));
    return env.addSource(consumer).map(new ExtractDetailMapFunction<>(clazz)).returns(clazz);
  }

  protected final SinkFunction<Event> sink(String topic) {
    // Cannot use exactly-once, because kafka-python consumer does not support
    // transactions
    return new FlinkKafkaProducer<>(
        topic, new EventSerializationSchema(topic), kafkaProps, Semantic.AT_LEAST_ONCE);
  }

  protected static Event output(Message details) {
    return output(details, (Instant) null);
  }

  protected static Event output(Message details, TimeWindow window) {
    return output(details, Instant.ofEpochMilli(window.getEnd()));
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

  protected abstract void setup() throws Exception;
}
