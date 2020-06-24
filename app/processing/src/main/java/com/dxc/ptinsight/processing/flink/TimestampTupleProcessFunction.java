package com.dxc.ptinsight.processing.flink;

import java.time.Instant;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class TimestampTupleProcessFunction<T> extends ProcessFunction<T, Tuple2<Instant, T>> {

  @Override
  public void processElement(T value, Context ctx, Collector<Tuple2<Instant, T>> out) {
    out.collect(Tuple2.of(Instant.ofEpochMilli(ctx.timestamp()), value));
  }
}
