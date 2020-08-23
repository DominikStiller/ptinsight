package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.analytics.HslRealtime.EmergencyStopDetectionResult;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import java.util.List;
import java.util.Map;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects emergency stops by checking if a vehicle decelerates from over 10 m/s to less than 1 m/s
 * within 10 seconds
 */
public class EmergencyStopDetectionStreamingJob extends Job {

  private static final Logger LOG =
      LoggerFactory.getLogger(EmergencyStopDetectionStreamingJob.class);

  public EmergencyStopDetectionStreamingJob() {
    super("Emergency Stop Detection (Streaming)");
  }

  @Override
  protected void setup() {
    var emergencyStopPattern =
        Pattern.<VehiclePosition>begin("cruising", AfterMatchSkipStrategy.skipPastLastEvent())
            .where(
                new SimpleCondition<>() {
                  @Override
                  public boolean filter(VehiclePosition value) {
                    return value.getSpeed() >= 10;
                  }
                })
            .next("braking")
            .oneOrMore()
            .where(
                new SimpleCondition<>() {
                  @Override
                  public boolean filter(VehiclePosition value) {
                    return value.getAcceleration() < 0;
                  }
                })
            .next("stopped")
            .where(
                new SimpleCondition<>() {
                  @Override
                  public boolean filter(VehiclePosition value) {
                    return value.getSpeed() < 1;
                  }
                })
            .within(Time.seconds(10));

    var vehiclePositionStream =
        source("input.vehicle-position", VehiclePosition.class)
            .keyBy(UniqueVehicleIdKeySelector.ofVehiclePosition());

    CEP.pattern(vehiclePositionStream, emergencyStopPattern)
        .process(new OutputProcessFunction())
        .addSink(sink("analytics.emergency-stop-detection-streaming"));
  }

  private static class OutputProcessFunction
      extends PatternProcessFunction<VehiclePosition, Event> {

    @Override
    public void processMatch(
        Map<String, List<VehiclePosition>> match, Context ctx, Collector<Event> out) {
      var cruising = match.get("cruising").get(0);
      var stopped = match.get("stopped").get(0);

      var maxDeceleration =
          match.get("braking").stream().mapToDouble(VehiclePosition::getAcceleration).min();

      var details =
          EmergencyStopDetectionResult.newBuilder()
              .setLatitude(stopped.getLatitude())
              .setLongitude(stopped.getLongitude())
              .setSpeedDiff(cruising.getSpeed() - stopped.getSpeed())
              .setMaxDeceleration((float) maxDeceleration.orElse(0))
              .setVehicleType(stopped.getVehicle().getType())
              .build();
      out.collect(output(details, ctx.timestamp()));
    }
  }
}
