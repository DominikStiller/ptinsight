// AUTO-GENERATED, DO NOT EDIT!
package com.dxc.ptinsight.proto;

import com.dxc.ptinsight.proto.Base;
import com.dxc.ptinsight.proto.egress.Counts;
import com.dxc.ptinsight.proto.ingress.HslRealtime;

import static com.google.protobuf.util.JsonFormat.TypeRegistry;

public class Registry {
  public static final TypeRegistry INSTANCE = TypeRegistry.newBuilder()
    .add(Base.getDescriptor().getMessageTypes())
    .add(Counts.getDescriptor().getMessageTypes())
    .add(HslRealtime.getDescriptor().getMessageTypes())
    .build();
}
