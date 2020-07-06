package com.dxc.ptinsight.processing.playground;

import java.util.List;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/** Experiment with late data and when they trigger window re-evaluation or side outputs */
public class LateDataHandling {

  public static void main(String[] args) throws Exception {
    var env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    var lateTag = new OutputTag<String>("late") {};
    var stream =
        env.addSource(new LateDataGenerator())
            .timeWindowAll(Time.seconds(3))
            .allowedLateness(Time.seconds(4))
            .sideOutputLateData(lateTag)
            .process(
                new ProcessAllWindowFunction<>() {
                  @Override
                  public void process(
                      Context context, Iterable<String> elements, Collector<Object> out) {
                    System.out.println("WINDOW(" + context.window() + ")");
                    elements.forEach(System.out::println);
                  }
                });
    stream.addSink(new DiscardingSink<>());

    stream
        .getSideOutput(lateTag)
        .process(
            new ProcessFunction<>() {
              @Override
              public void processElement(String value, Context ctx, Collector<Object> out) {
                System.out.println("SIDEOUT: " + value);
              }
            })
        .addSink(new DiscardingSink<>());

    env.execute();
  }

  private static class LateDataGenerator implements SourceFunction<String> {

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
      var e =
          List.of(
              Tuple2.of(1, "foo1"),
              Tuple2.of(2, "foo2"),
              Tuple2.of(3, "foo3"),
              Tuple2.of(4, "foo4"),
              Tuple2.of(5, "foo5"),
              Tuple2.of(6, "foo6"),
              Tuple2.of(2, "firstLate"),
              Tuple2.of(2, "secondLate"),
              Tuple2.of(7, "foo7"),
              Tuple2.of(2, "thirdLate"));

      var i = 0;
      while (i < e.size()) {
        var x = e.get(i++);
        ctx.collectWithTimestamp(x.f1, x.f0 * 1000);
        ctx.emitWatermark(new Watermark(x.f0 * 1000));
        Thread.sleep(1000);
      }
    }

    @Override
    public void cancel() {}
  }
}
