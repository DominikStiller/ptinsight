package com.dxc.ptinsight.processing.playground;

import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * Experiment with two keyed windows with different keys
 *
 * <p>For example, first key by vehicle for most-recent deduplication, then by geocell for
 * aggregation
 */
public class MultiWindowing {

  public static void main(String[] args) throws Exception {
    var env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    env.addSource(new DataGenerator())
        .keyBy(value -> value.f0)
        .timeWindow(Time.seconds(5))
        .evictor(MostRecentDeduplicationEvictor.of(value -> value))
        .process(
            new ProcessWindowFunction<
                Tuple2<String, String>, Tuple2<String, String>, String, TimeWindow>() {
              @Override
              public void process(
                  String key,
                  Context context,
                  Iterable<Tuple2<String, String>> elements,
                  Collector<Tuple2<String, String>> out) {
                var count = new AtomicInteger();
                elements.forEach(e -> count.getAndIncrement());
                System.out.println(key + ": " + count);
                elements.forEach(out::collect);
              }
            })
        .keyBy(value -> value.f1)
        .timeWindow(Time.seconds(5))
        .process(
            new ProcessWindowFunction<
                Tuple2<String, String>, Tuple2<String, String>, String, TimeWindow>() {
              @Override
              public void process(
                  String key,
                  Context context,
                  Iterable<Tuple2<String, String>> elements,
                  Collector<Tuple2<String, String>> out) {
                var count = new AtomicInteger();
                elements.forEach(e -> count.getAndIncrement());
                System.out.println(key + ": " + count);
              }
            })
        .addSink(new DiscardingSink<>());

    env.execute();
  }

  private static class DataGenerator implements SourceFunction<Tuple2<String, String>> {

    @Override
    public void run(SourceContext<Tuple2<String, String>> ctx) throws Exception {
      var e =
          List.of(
              Tuple2.of("foo1", "bar1"),
              Tuple2.of("foo1", "bar1"),
              Tuple2.of("foo2", "bar1"),
              Tuple2.of("foo2", "bar2"),
              Tuple2.of("foo3", "bar2"),
              Tuple2.of("foo3", "bar2"));

      var i = 0;
      while (i < e.size()) {
        ctx.collectWithTimestamp(e.get(i++), System.currentTimeMillis());
        ctx.emitWatermark(new Watermark(System.currentTimeMillis()));
        Thread.sleep(50);
      }
    }

    @Override
    public void cancel() {}
  }
}
