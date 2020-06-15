package com.dxc.ptinsight.processing.jobs;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.proto.Base.VehicleType;
import com.dxc.ptinsight.proto.egress.Counts.ArrivalCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.Arrival;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class ArrivalCountJob extends Job {

  public ArrivalCountJob() {
    super("Arrival Counter");
  }

  @Override
  protected void setup() {
    source("ingress.arrival", Arrival.class)
        .keyBy((KeySelector<Arrival, VehicleType>) value -> value.getVehicle().getType())
        .timeWindow(Time.seconds(1))
        .process(new ArrivalCounterProcessFunction())
        .addSink(sink("egress.arrival-count"));
  }

  private static class ArrivalCounterProcessFunction
      extends ProcessWindowFunction<Arrival, Event, VehicleType, TimeWindow> {
    @Override
    public void process(
        VehicleType key, Context context, Iterable<Arrival> elements, Collector<Event> out) {
      AtomicInteger count = new AtomicInteger();
      elements.forEach((e -> count.getAndIncrement()));

      var details =
          ArrivalCount.newBuilder()
              .setVehicleType(key)
              .setCount(count.intValue())
              .setWindowStart(
                  Timestamps.fromInstant(Instant.ofEpochMilli(context.window().getStart())))
              .setWindowEnd(Timestamps.fromInstant(Instant.ofEpochMilli(context.window().getEnd())))
              .build();
      out.collect(output(details, Instant.ofEpochMilli(context.window().getStart())));
    }
  }
}
