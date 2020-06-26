package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.processing.flink.FuzzyTripFinalStopLookupAsyncFunction;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import com.dxc.ptinsight.processing.flink.TimestampTupleProcessFunction;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.egress.Counts.FinalStopCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Find the most visited final stops of ongoing trips */
public class FinalStopCountJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(FinalStopCountJob.class);

  public FinalStopCountJob() {
    super("Final Stop Count");
  }

  @Override
  protected void setup() {
    var input =
        source("ingress.vehicle-position", VehiclePosition.class)
            .process(new TimestampTupleProcessFunction<>());

    // Requests usually take between 2 and 3 s, but can be up to 15 s
    // There are about 1000 requests per second
    // Capacity of 1000 is only required initially when cache is not yet filled
    AsyncDataStream.unorderedWait(
            input, new FuzzyTripFinalStopLookupAsyncFunction(), 5, TimeUnit.SECONDS, 1000)
        // For some reason, event time window triggers are not executed after an async function
        // Sliding windows with intervals < 10 s create too much backpressure
        .windowAll(SlidingProcessingTimeWindows.of(Time.minutes(5), Time.seconds(10)))
        .evictor(
            new MostRecentDeduplicationEvictor<>(
                UniqueVehicleIdKeySelector.ofVehiclePosition().inTuple(0)))
        .process(new FinalStopCounterProcessFunction())
        .addSink(sink("egress.final-stop-count"));
    // TODO restructure to aggregate function to keep state small
  }

  private static class FinalStopCounterProcessFunction
      extends ProcessAllWindowFunction<Tuple2<VehiclePosition, Long>, Event, TimeWindow> {

    @Override
    public void process(
        Context context, Iterable<Tuple2<VehiclePosition, Long>> elements, Collector<Event> out) {
      var counts = new HashMap<Long, Integer>();
      elements.forEach(e -> counts.merge(e.f1, 1, Integer::sum));

      for (var entry : counts.entrySet()) {
        var details =
            FinalStopCount.newBuilder()
                .setGeocell(entry.getKey())
                .setCount(entry.getValue())
                .build();
        out.collect(output(details, context.window()));
      }
    }
  }
}
