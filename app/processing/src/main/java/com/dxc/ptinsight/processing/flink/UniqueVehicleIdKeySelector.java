package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.proto.ingress.HslRealtime.VehicleInfo;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

/** Selector for the unique identifier of a vehicle */
public class UniqueVehicleIdKeySelector<T> implements KeySelector<T, Long> {

  private final MapFunction<T, VehicleInfo> vehicleInfoSelector;

  private UniqueVehicleIdKeySelector(MapFunction<T, VehicleInfo> vehicleInfoSelector) {
    this.vehicleInfoSelector = vehicleInfoSelector;
  }

  @Override
  public Long getKey(T value) throws Exception {
    var vehicle = vehicleInfoSelector.map(value);
    // The combination of operator id and vehicle number uniquely identifies a vehicle
    return (((long) vehicle.getOperator()) << 32) | (vehicle.getNumber() & 0xffffffffL);
  }

  public static UniqueVehicleIdKeySelector<VehiclePosition> ofVehiclePosition() {
    return new UniqueVehicleIdKeySelector<>(VehiclePosition::getVehicle);
  }

  public UniqueVehicleIdKeySelector<Tuple2<?, ?>> inTuple(int index) {
    return new UniqueVehicleIdKeySelector<>(
        value -> vehicleInfoSelector.map(value.getField(index)));
  }
}
