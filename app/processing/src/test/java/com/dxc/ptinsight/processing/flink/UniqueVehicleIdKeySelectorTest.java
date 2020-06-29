package com.dxc.ptinsight.processing.flink;

import static org.junit.jupiter.api.Assertions.*;

import com.dxc.ptinsight.proto.ingress.HslRealtime.VehicleInfo;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.jupiter.api.Test;

class UniqueVehicleIdKeySelectorTest {

  @Test
  void shouldReturnIdenticalIdForIdenticalVehicles() throws Exception {
    var selector = UniqueVehicleIdKeySelector.<VehicleInfo>of(value -> value);

    var vehicleInfo1 = createVehicleInfo(42, 6938);
    var vehicleInfo2 = createVehicleInfo(42, 6938);

    assertEquals(selector.getKey(vehicleInfo1), selector.getKey(vehicleInfo2));
  }

  @Test
  void shouldReturnDifferentIdForDifferentVehicles() throws Exception {
    var selector = UniqueVehicleIdKeySelector.<VehicleInfo>of(value -> value);

    var vehicleInfo1 = createVehicleInfo(42, 6938);
    var vehicleInfo2 = createVehicleInfo(43, 6938);
    var vehicleInfo3 = createVehicleInfo(42, 6937);

    assertNotEquals(selector.getKey(vehicleInfo1), selector.getKey(vehicleInfo2));
    assertNotEquals(selector.getKey(vehicleInfo2), selector.getKey(vehicleInfo3));
    assertNotEquals(selector.getKey(vehicleInfo1), selector.getKey(vehicleInfo3));
  }

  @Test
  void shouldReturnCorrectIdFromVehiclePosition() throws Exception {
    var selector = UniqueVehicleIdKeySelector.ofVehiclePosition();

    var vehiclePosition1 = createVehiclePosition(42, 6938);
    var vehiclePosition2 = createVehiclePosition(42, 6938);
    var vehiclePosition3 = createVehiclePosition(42, 6939);

    assertEquals(selector.getKey(vehiclePosition1), selector.getKey(vehiclePosition2));
    assertNotEquals(selector.getKey(vehiclePosition1), selector.getKey(vehiclePosition3));
  }

  @Test
  void shouldReturnCorrectIdFromTuple() throws Exception {
    var selector = UniqueVehicleIdKeySelector.ofVehiclePosition().inTuple(1);

    var vehiclePosition1 = Tuple2.of(11, createVehiclePosition(42, 6938));
    var vehiclePosition2 = Tuple2.of(83, createVehiclePosition(42, 6938));
    var vehiclePosition3 = Tuple2.of(83, createVehiclePosition(42, 6939));

    assertEquals(selector.getKey(vehiclePosition1), selector.getKey(vehiclePosition2));
    assertNotEquals(selector.getKey(vehiclePosition1), selector.getKey(vehiclePosition3));
  }

  private VehicleInfo createVehicleInfo(int operator, int number) {
    return VehicleInfo.newBuilder().setOperator(operator).setNumber(number).build();
  }

  private VehiclePosition createVehiclePosition(int operator, int number) {
    return VehiclePosition.newBuilder().setVehicle(createVehicleInfo(operator, number)).build();
  }
}
