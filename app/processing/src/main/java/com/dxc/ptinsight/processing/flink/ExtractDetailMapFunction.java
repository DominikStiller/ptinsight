package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.proto.Base;
import com.google.protobuf.Message;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

public class ExtractDetailMapFunction<T extends Message> extends RichMapFunction<Base.Event, T>
    implements ResultTypeQueryable<T> {

  private final Class<T> clazz;

  public ExtractDetailMapFunction(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T map(Base.Event value) throws Exception {
    return value.getDetails().unpack(clazz);
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return TypeInformation.of(clazz);
  }
}
