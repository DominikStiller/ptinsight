package com.dxc.ptinsight.processing.jobs;

import static com.dxc.ptinsight.proto.Base.Event;

import com.dxc.ptinsight.processing.flink.CountAggregateFunction;
import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.IdentityProcessFunction;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.MostRecentDeduplicationEvictor;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.analytics.HslRealtime.VehicleDistributionResult;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Count the number of vehicles in each cell
 *
 * <p>If a vehicle was in multiple cells for a window, use only the last cell
 */
public class VehicleDistributionJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(VehicleDistributionJob.class);

  public VehicleDistributionJob() {
    super("Vehicle Distribution");
  }

  @Override
  protected void setup() {
    // See documentation for consecutive window operations:
    // https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/windows.html#consecutive-windowed-operations
    // Do not allow lateness, because evicted elements in the first window remain in the second
    // window when it is triggered again
    source("input.vehicle-position", VehiclePosition.class)
        // First, key by vehicle to select only most recent position of each vehicle
        .keyBy(UniqueVehicleIdKeySelector.ofVehiclePosition())
        .timeWindow(Time.seconds(30), Time.seconds(5))
        // Since stream is already keyed, use evict without specifying key
        .evictor(MostRecentDeduplicationEvictor.ofAll())
        // Collect most recent vehicle position from all keyed streams into single stream
        .process(new IdentityProcessFunction<>())
        // Then, key by geocell to count vehicles
        .keyBy(GeocellKeySelector.ofVehiclePosition())
        .timeWindow(Time.seconds(5))
        .aggregate(new CountAggregateFunction<>(), new OutputProcessFunction())
        .addSink(sink("analytics.vehicle-distribution"));
  }

  private static class OutputProcessFunction
      extends ProcessWindowFunction<Integer, Event, Long, TimeWindow> {

    @Override
    public void process(
        Long geocell, Context context, Iterable<Integer> elements, Collector<Event> out) {
      // Iterable only contains the result of the aggregate function as single element
      var count = elements.iterator().next();
      var details =
          VehicleDistributionResult.newBuilder().setGeocell(geocell).setCount(count).build();
      out.collect(output(details, context.window()));
    }
  }
}
