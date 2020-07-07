package com.dxc.ptinsight.processing.flink;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.apache.flink.util.Collector;

public class TimestampValueProcessFunction<T, KEY, W extends Window>
    extends ProcessWindowFunction<T, TimestampedValue<T>, KEY, W> {

  @Override
  public void process(
      KEY key, Context context, Iterable<T> elements, Collector<TimestampedValue<T>> out) {
    var timestamp = context.window().maxTimestamp();
    elements.forEach(e -> out.collect(new TimestampedValue<T>(e, timestamp)));
  }
}
