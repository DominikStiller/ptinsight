package com.dxc.ptinsight.processing.jobs;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.egress.Counts.VehicleCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.io.IOException;
import java.util.HashMap;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Count the number of vehicles in each cell If a vehicle was in multiple cells for a window, use
 * only the last cell
 */
public class VehicleCountJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(VehicleCountJob.class);

  public VehicleCountJob() throws IOException {
    super("Vehicle Counter");
  }

  @Override
  protected void setup() {
    // Cannot use keyed window because deduplication needs to be applied to all cells
    source("ingress.vehicle-position", VehiclePosition.class)
        .windowAll(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(5)))
        .evictor(
            new MostRecentDeduplicationEvictor<>(UniqueVehicleIdKeySelector.ofVehiclePosition()))
        .process(new VehicleCounterProcessFunction())
        .addSink(sink("egress.vehicle-count"));
    // TODO use staggered window when available: https://github.com/apache/flink/pull/12297
  }

  private static class VehicleCounterProcessFunction
      extends ProcessAllWindowFunction<VehiclePosition, Event, TimeWindow> {

    private transient GeocellKeySelector<VehiclePosition> cellSelector;

    @Override
    public void open(Configuration parameters) throws Exception {
      cellSelector = GeocellKeySelector.ofVehiclePosition();
    }

    @Override
    public void process(Context context, Iterable<VehiclePosition> elements, Collector<Event> out) {
      var counts = new HashMap<Long, Integer>();
      elements.forEach(
          e -> {
            try {
              var geocell = cellSelector.getKey(e);
              counts.merge(geocell, 1, Integer::sum);
            } catch (Exception exception) {
              LOG.error("Could not extract geocell", exception);
            }
          });

      for (var entry : counts.entrySet()) {
        var details =
            VehicleCount.newBuilder().setGeocell(entry.getKey()).setCount(entry.getValue()).build();
        out.collect(output(details, context.window()));
      }
    }
  }
}
