package com.dxc.ptinsight.processing.flink;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

public class IdentityProcessFunction<T, KEY, W extends Window>
    extends ProcessWindowFunction<T, T, KEY, W> {

  @Override
  public void process(KEY aLong, Context context, Iterable<T> elements, Collector<T> out) {
    elements.forEach(out::collect);
  }
}
