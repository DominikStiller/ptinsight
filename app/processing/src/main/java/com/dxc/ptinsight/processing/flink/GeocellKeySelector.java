package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.Geocells;
import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeocellKeySelector<T> implements KeySelector<T, Long> {

  private static final Logger LOG = LoggerFactory.getLogger(GeocellKeySelector.class);

  private final MapFunction<T, Tuple2<Float, Float>> coordinateSelector;

  private GeocellKeySelector(MapFunction<T, Tuple2<Float, Float>> coordinateSelector) {
    this.coordinateSelector = coordinateSelector;
  }

  @Override
  public Long getKey(T value) throws Exception {
    var coordinates = coordinateSelector.map(value);
    return Geocells.h3()
        .geoToH3(coordinates.f0, coordinates.f1, EntryPoint.getConfiguration().h3.resolution);
  }

  public static GeocellKeySelector<Tuple3<Float, Float, Long>> ofTuple3() {
    return new GeocellKeySelector<>(
        (MapFunction<Tuple3<Float, Float, Long>, Tuple2<Float, Float>>)
            tuple -> Tuple2.of(tuple.getField(0), tuple.getField(1)));
  }

  public static GeocellKeySelector<VehiclePosition> ofVehiclePosition() {
    return new GeocellKeySelector<>(
        (MapFunction<VehiclePosition, Tuple2<Float, Float>>)
            pos -> Tuple2.of(pos.getLatitude(), pos.getLongitude()));
  }
}
