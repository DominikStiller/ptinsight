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
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
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

    source("ingress.vehicle-position", VehiclePosition.class)
        .keyBy(
            (KeySelector<VehiclePosition, Long>)
                value ->
                    h3.geoToH3(
                        value.getLatitude(),
                        value.getLongitude(),
                        EntryPoint.getConfiguration().h3.resolution))
        .window(SlidingEventTimeWindows.of(Time.minutes(1), Time.seconds(5)))
        .evictor(new MostRecentDeduplicationEvictor<>(new UniqueVehicleIdKeySelector()))
        .process(new VehicleCounterProcessFunction())
        .addSink(sink("egress.vehicle-count"));
  }

  private static class VehicleCounterProcessFunction
      extends ProcessWindowFunction<VehiclePosition, Event, Long, TimeWindow> {
    @Override
    public void process(
        Long key, Context context, Iterable<VehiclePosition> elements, Collector<Event> out) {
      AtomicInteger count = new AtomicInteger();
      elements.forEach((e -> count.getAndIncrement()));

      var details = VehicleCount.newBuilder().setH3Index(key).setCount(count.intValue()).build();
      out.collect(output(details, context.window()));
    }
  }
}
