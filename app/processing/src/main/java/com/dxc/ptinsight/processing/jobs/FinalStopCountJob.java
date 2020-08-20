package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.processing.flink.CountAggregateFunction;
import com.dxc.ptinsight.processing.flink.FuzzyTripFinalStopLookupAsyncFunction;
import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import com.dxc.ptinsight.processing.flink.TimestampValueProcessFunction;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.analytics.Counts.FinalStopCount;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import java.util.concurrent.TimeUnit;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Find the most visited final stops of ongoing trips */
public class FinalStopCountJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(FinalStopCountJob.class);

  public FinalStopCountJob() {
    super("Final Stop Counter");
  }

  @Override
  protected void setup() {
    var vehiclePositionStream =
        source("input.vehicle-position", VehiclePosition.class)
            .keyBy(UniqueVehicleIdKeySelector.ofVehiclePosition())
            .timeWindow(Time.minutes(5), Time.seconds(5))
            .evictor(MostRecentDeduplicationEvictor.ofAll())
            .process(new TimestampValueProcessFunction<>());

    // Requests usually take between 2 and 3 s, but can be up to 15 s
    // There are about 100 requests per second after deduplication
    // Capacity is 200 to handle spikes
    AsyncDataStream.unorderedWait(
            vehiclePositionStream,
            new FuzzyTripFinalStopLookupAsyncFunction(),
            5,
            TimeUnit.SECONDS,
            200)
        .keyBy(GeocellKeySelector.ofTuple2())
        .timeWindow(Time.seconds(5))
        .aggregate(new CountAggregateFunction<>(), new OutputProcessFunction())
        .addSink(sink("analytics.final-stop-count"));
  }

  private static class OutputProcessFunction
      extends ProcessWindowFunction<Integer, Event, Long, TimeWindow> {

    @Override
    public void process(
        Long geocell, Context context, Iterable<Integer> elements, Collector<Event> out) {
      var count = elements.iterator().next();
      var details = FinalStopCount.newBuilder().setGeocell(geocell).setCount(count).build();
      out.collect(output(details, context.window()));
    }
  }
}
