package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.Geocells;
import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.egress.Flow.FlowDirection;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the most traversed edge for each cell, i.e. the neighbor cell that that the most vehicles
 * from a cell travel to
 */
public class FlowDirectionJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(FlowDirectionJob.class);

  public FlowDirectionJob() {
    super("Flow Direction");
  }

  @Override
  protected void setup() {
    source("ingress.vehicle-position", VehiclePosition.class)
        .keyBy(UniqueVehicleIdKeySelector.ofVehiclePosition())
        .process(new CellChangeDetectionProcessFunction())
        .keyBy(value -> value.f0)
        .timeWindow(Time.minutes(5), Time.seconds(5))
        .allowedLateness(Time.seconds(5))
        .process(new FindMostTraversedEdgeProcessFunction())
        .addSink(sink("egress.flow-direction"));
  }

  protected static class CellChangeDetectionProcessFunction
      extends KeyedProcessFunction<Long, VehiclePosition, Tuple2<Long, Long>> {
    private transient ValueState<Long> lastCellState;
    private transient GeocellKeySelector<VehiclePosition> cellSelector;

    @Override
    public void open(Configuration parameters) {
      var lastCellStateDescriptor = new ValueStateDescriptor<>("lastCellState", Types.LONG);
      lastCellState = getRuntimeContext().getState(lastCellStateDescriptor);

      this.cellSelector = GeocellKeySelector.ofVehiclePosition();
    }

    @Override
    public void processElement(
        VehiclePosition value, Context ctx, Collector<Tuple2<Long, Long>> out) throws Exception {
      var lastCell = lastCellState.value();
      var currentCell = this.cellSelector.getKey(value);

      if (lastCell != null
          && !lastCell.equals(currentCell)
          // H3 edges only allow neighboring cells
          && Geocells.h3().h3IndexesAreNeighbors(lastCell, currentCell)) {
        out.collect(Tuple2.of(lastCell, currentCell));
      }

      lastCellState.update(currentCell);
    }
  }

  protected static class FindMostTraversedEdgeProcessFunction
      extends ProcessWindowFunction<Tuple2<Long, Long>, Event, Long, TimeWindow> {
    @Override
    public void process(
        Long key, Context context, Iterable<Tuple2<Long, Long>> elements, Collector<Event> out) {
      var counts = new HashMap<Long, Integer>();
      elements.forEach(e -> counts.merge(e.f1, 1, Integer::sum));

      counts.entrySet().stream()
          .max(Entry.comparingByValue())
          .ifPresent(
              targetCell -> {
                var edge = Geocells.h3().getH3UnidirectionalEdge(key, targetCell.getKey());
                var details =
                    FlowDirection.newBuilder()
                        .setGeocellsEdge(edge)
                        .setCount(targetCell.getValue())
                        .build();
                out.collect(output(details, context.window()));
              });
    }
  }
}
