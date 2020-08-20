package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.Geocells;
import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.util.Collector;

/**
 * Splits a stream with location information into keyed streams for every center cell of the 1-rings
 * it is contained in
 *
 * @see <a href=https://h3geo.org/docs/api/traversal>kRing</a>
 */
public class GeocellRingSplitter<T> {

  private final GeocellRingSideOutputFlatMapFunction<T> splitFunction;

  private GeocellRingSplitter(MapFunction<T, Tuple2<Float, Float>> coordinateSelector) {
    this.splitFunction = new GeocellRingSideOutputFlatMapFunction<>(coordinateSelector);
  }

  public KeyedStream<Tuple3<String, String, T>, String> split(DataStream<T> stream) {
    return stream.flatMap(this.splitFunction).keyBy(value -> value.f1);
  }

  static class GeocellRingSideOutputFlatMapFunction<T>
      implements FlatMapFunction<T, Tuple3<String, String, T>> {

    private final MapFunction<T, Tuple2<Float, Float>> coordinateSelector;

    private GeocellRingSideOutputFlatMapFunction(
        MapFunction<T, Tuple2<Float, Float>> coordinateSelector) {
      this.coordinateSelector = coordinateSelector;
    }

    @Override
    public void flatMap(T value, Collector<Tuple3<String, String, T>> out) throws Exception {
      var h3 = Geocells.h3();
      var coordinates = coordinateSelector.map(value);
      var ringCenter =
          h3.geoToH3Address(
              coordinates.f0, coordinates.f1, EntryPoint.getConfiguration().h3.resolution);
      h3.kRing(ringCenter, 1).forEach(cell -> out.collect(Tuple3.of(ringCenter, cell, value)));
    }
  }

  public static GeocellRingSplitter<VehiclePosition> ofVehiclePosition() {
    return new GeocellRingSplitter<>(
        (MapFunction<VehiclePosition, Tuple2<Float, Float>>)
            pos -> Tuple2.of(pos.getLatitude(), pos.getLongitude()));
  }
}
