package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.processing.EntryPoint;
import com.uber.h3core.H3Core;
import java.io.IOException;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeocellKeySelector<T> implements KeySelector<T, Long> {

  private static final Logger LOG = LoggerFactory.getLogger(GeocellKeySelector.class);

  private static transient H3Core h3;
  private final MapFunction<T, Tuple2<Float, Float>> coordinateSelector;

  static {
    try {
      h3 = H3Core.newInstance();
    } catch (IOException e) {
      LOG.error("Could not create H3 instance", e);
    }
  }

  private GeocellKeySelector(MapFunction<T, Tuple2<Float, Float>> coordinateSelector) {
    this.coordinateSelector = coordinateSelector;
  }

  @Override
  public Long getKey(T value) throws Exception {
    var coordinates = coordinateSelector.map(value);
    return h3.geoToH3(coordinates.f0, coordinates.f1, EntryPoint.getConfiguration().h3.resolution);
  }

  public static GeocellKeySelector<Tuple3<Float, Float, Long>> ofTuple3() {
    return new GeocellKeySelector<>(
        (MapFunction<Tuple3<Float, Float, Long>, Tuple2<Float, Float>>)
            tuple -> Tuple2.of(tuple.getField(0), tuple.getField(1)));
  }
}
