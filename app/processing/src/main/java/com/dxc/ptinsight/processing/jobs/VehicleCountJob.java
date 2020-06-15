package com.dxc.ptinsight.processing.jobs;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.egress.Counts.VehicleCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import com.uber.h3core.H3Core;
import java.io.IOException;
import java.util.HashMap;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleCountJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(VehicleCountJob.class);

  private static transient H3Core h3;

  static {
    try {
      h3 = H3Core.newInstance();
    } catch (IOException e) {
      LOG.error("Could not create H3 instance", e);
    }
  }

  public VehicleCountJob() throws IOException {
    super("Vehicle Counter");
  }

  @Override
  protected void setup() {
    // Cannot use keyed window because deduplication needs to be applied to all cells
    source("ingress.vehicle-position", VehiclePosition.class)
        .windowAll(SlidingEventTimeWindows.of(Time.minutes(1), Time.seconds(5)))
        .evictor(new MostRecentDeduplicationEvictor<>(new UniqueVehicleIdKeySelector()))
        .process(new VehicleCounterProcessFunction())
        .addSink(sink("egress.vehicle-count"));
  }

  private static class VehicleCounterProcessFunction
      extends ProcessAllWindowFunction<VehiclePosition, Event, TimeWindow> {

    @Override
    public void process(Context context, Iterable<VehiclePosition> elements, Collector<Event> out) {
      var counts = new HashMap<Long, Integer>();
      elements.forEach(
          e -> {
            var h3index =
                h3.geoToH3(
                    e.getLatitude(), e.getLongitude(), EntryPoint.getConfiguration().h3.resolution);
            counts.merge(h3index, 1, Integer::sum);
          });

      for (var entry : counts.entrySet()) {
        //        System.out.println(String.format("H3: %s   Cnt: %d", entry.getKey(),
        // entry.getValue()));
        var details =
            VehicleCount.newBuilder().setH3Index(entry.getKey()).setCount(entry.getValue()).build();
        out.collect(output(details, context.window()));
      }
    }
  }
}
