package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.proto.input.HslRealtime.VehicleInfo;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;

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

  public static <T> UniqueVehicleIdKeySelector<T> of(
      MapFunction<T, VehicleInfo> vehicleInfoSelector) {
    return new UniqueVehicleIdKeySelector<>(vehicleInfoSelector);
  }

  public static UniqueVehicleIdKeySelector<VehiclePosition> ofVehiclePosition() {
    return new UniqueVehicleIdKeySelector<>(VehiclePosition::getVehicle);
  }

  public static UniqueVehicleIdKeySelector<VehicleInfo> ofVehicleInfo() {
    return new UniqueVehicleIdKeySelector<>(x -> x);
  }

  public UniqueVehicleIdKeySelector<Tuple> inTuple(int index) {
    return new UniqueVehicleIdKeySelector<>(
        value -> vehicleInfoSelector.map(value.getField(index)));
  }
}
