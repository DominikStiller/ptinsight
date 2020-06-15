package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import org.apache.flink.api.java.functions.KeySelector;

/** Selector for the unique identifier of a vehicle */
public class UniqueVehicleIdKeySelector implements KeySelector<VehiclePosition, Long> {

  @Override
  public Long getKey(VehiclePosition value) {
    var vehicle = value.getVehicle();
    // The combination of operator id and vehicle number uniquely identifies a vehicle
    return (((long) vehicle.getOperator()) << 32) | (vehicle.getNumber() & 0xffffffffL);
  }
}
