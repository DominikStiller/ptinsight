package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.proto.Base;
import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.RichMapFunction;

public class ExtractDetailMapFunction<T extends Message> extends RichMapFunction<Base.Event, T> {

  private final Class<T> clazz;

  public ExtractDetailMapFunction(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T map(Base.Event value) throws Exception {
    return value.getDetails().unpack(this.clazz);
  }
}
