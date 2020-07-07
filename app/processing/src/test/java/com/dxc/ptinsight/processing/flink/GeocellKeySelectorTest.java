package com.dxc.ptinsight.processing.flink;

import static org.junit.jupiter.api.Assertions.*;

import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeocellKeySelectorTest {

  @BeforeEach
  void setUp() {
    EntryPoint.getConfiguration().h3.resolution = 5;
  }

  @Test
  void shouldReturnCorrectGeocell() throws Exception {
    var selector = GeocellKeySelector.<Tuple2<Float, Float>>of(value -> value);

    var actual = selector.getKey(Tuple2.of(40.689167f, -74.044444f));
    assertEquals(599718752904282111L, actual);
  }

  @Test
  void shouldReturnCorrectGeocellFromTuple2() throws Exception {
    var selector = GeocellKeySelector.ofTuple2();

    var actual = selector.getKey(Tuple2.of(40.689167, -74.044444));
    assertEquals(599718752904282111L, actual);
  }

  @Test
  void shouldReturnCorrectGeocellFromTuple3() throws Exception {
    var selector = GeocellKeySelector.ofTuple3();

    var actual = selector.getKey(Tuple3.of(40.689167f, -74.044444f, 42L));
    assertEquals(599718752904282111L, actual);
  }

  @Test
  void shouldReturnCorrectGeocellFromVehiclePositionInTuple() throws Exception {
    var selector = GeocellKeySelector.ofVehiclePosition().inTuple(1);

    var vehiclePosition =
        VehiclePosition.newBuilder().setLatitude(40.689167f).setLongitude(-74.044444f).build();

    var actual = selector.getKey(Tuple2.of(42, vehiclePosition));
    assertEquals(599718752904282111L, actual);
  }
}
