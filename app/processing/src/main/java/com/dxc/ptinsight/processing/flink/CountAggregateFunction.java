package com.dxc.ptinsight.processing.flink;

import org.apache.flink.api.common.functions.AggregateFunction;

public class CountAggregateFunction<T> implements AggregateFunction<T, Integer, Integer> {
  @Override
  public Integer createAccumulator() {
    return 0;
  }

  @Override
  public Integer add(T value, Integer accumulator) {
    return accumulator + 1;
  }

  @Override
  public Integer getResult(Integer accumulator) {
    return accumulator;
  }

  @Override
  public Integer merge(Integer a, Integer b) {
    return a + b;
  }
}
