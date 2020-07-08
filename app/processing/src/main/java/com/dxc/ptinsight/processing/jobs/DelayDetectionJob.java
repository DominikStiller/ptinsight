package com.dxc.ptinsight.processing.jobs;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.proto.egress.Delays.DelayStatistics;
import com.dxc.ptinsight.proto.ingress.HslRealtime.Arrival;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Get statistics of arrival delays for each cell */
public class DelayDetectionJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(DelayDetectionJob.class);

  public DelayDetectionJob() {
    super("Delay Detection");
  }

  @Override
  protected void setup() {
    source("ingress.arrival", Arrival.class)
        .process(new DelayCalculatorProcessFunction())
        .keyBy(GeocellKeySelector.ofTuple3())
        .timeWindow(Time.minutes(5), Time.seconds(5))
        .allowedLateness(Time.seconds(5))
        .process(new DelayStatisticsProcessFunction())
        .addSink(sink("egress.delay-statistics"));
  }

  protected static class DelayCalculatorProcessFunction
      extends ProcessFunction<Arrival, Tuple3<Float, Float, Long>> {

    @Override
    public void processElement(
        Arrival value, Context ctx, Collector<Tuple3<Float, Float, Long>> out) {
      var scheduled = Timestamps.toInstant(value.getScheduledArrival());
      // Calulations are in minutes because that is the schedule resolution
      var actual = Instant.ofEpochMilli(ctx.timestamp()).truncatedTo(ChronoUnit.MINUTES);
      var delay = Duration.between(scheduled, actual).toMinutes();

      out.collect(Tuple3.of(value.getLatitude(), value.getLongitude(), delay));
    }
  }

  protected static class DelayStatisticsProcessFunction
      extends ProcessWindowFunction<Tuple3<Float, Float, Long>, Event, Long, TimeWindow> {

    @Override
    public void process(
        Long key,
        Context context,
        Iterable<Tuple3<Float, Float, Long>> elements,
        Collector<Event> out) {
      var sorted = new ArrayList<Long>();
      elements.forEach(e -> sorted.add(e.f2));
      Collections.sort(sorted);

      var details =
          DelayStatistics.newBuilder()
              .setGeocell(key)
              .setPercentile50Th(getNthPercentile(sorted, 50))
              .setPercentile90Th(getNthPercentile(sorted, 90))
              .setPercentile99Th(getNthPercentile(sorted, 99))
              .build();
      out.collect(output(details, context.window()));
    }

    private long getNthPercentile(List<Long> values, int n) {
      var index = (int) Math.ceil((n / 100.0) * values.size());
      return values.get(index - 1);
    }
  }
}
