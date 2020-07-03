package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.Resources;
import com.dxc.ptinsight.processing.flink.GeocellKeySelector;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.Base.VehicleType;
import com.dxc.ptinsight.proto.egress.Patterns.EmergencyStopCount;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.Slide;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects emergency stops by checking if a vehicle decelerates from over 10 m/s to less than 1 m/s within 10 seconds
 */
public class EmergencyStopDetectionJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(EmergencyStopDetectionJob.class);

  public EmergencyStopDetectionJob() {
    super("Emergency Stop Detection");
  }

  @Override
  protected void setup() throws IOException {
    var tableEnv = getTableEnvironment();

    // Set up tables from stream
    var vehiclePositionStream =
        source("ingress.vehicle-position", VehiclePosition.class)
            .map(new VehiclePositionTableTupleBuilderProcessFunction());
    var vehiclePositionTable =
        tableEnv.fromDataStream(
            vehiclePositionStream,
            "vehicle_id, vehicle_type, geocell, speed, acceleration, event_time.rowtime");
    tableEnv.createTemporaryView("vehicle_position", vehiclePositionTable);

    // Detect emergency stops and count stops per cell
    var stopCountTable =
        tableEnv
            .sqlQuery(Resources.getContents("sql/detectEmergencyStop.sql"))
            .window(Slide.over("5.minutes").every("5.seconds").on("stop_time").as("w"))
            .groupBy("w, geocell")
            .select("geocell, geocell.count, speed_diff.avg, max_deceleration.min, w.end");

    // Convert result table back to stream
    tableEnv
        .toAppendStream(stopCountTable, Row.class)
        .process(new RowToEventProcessFunction())
        .addSink(sink("egress.emergency-stop-count"));
  }

  private static class VehiclePositionTableTupleBuilderProcessFunction
      extends RichMapFunction<VehiclePosition, Tuple5<Long, VehicleType, Long, Float, Float>> {

    private transient GeocellKeySelector<VehiclePosition> geocellSelector;
    private transient UniqueVehicleIdKeySelector<VehiclePosition> vehicleIdSelector;

    @Override
    public void open(Configuration parameters) throws Exception {
      geocellSelector = GeocellKeySelector.ofVehiclePosition();
      vehicleIdSelector = UniqueVehicleIdKeySelector.ofVehiclePosition();
    }

    @Override
    public Tuple5<Long, VehicleType, Long, Float, Float> map(VehiclePosition value)
        throws Exception {
      return Tuple5.of(
          vehicleIdSelector.getKey(value),
          value.getVehicle().getType(),
          geocellSelector.getKey(value),
          value.getSpeed(),
          value.getAcceleration());
    }
  }

  private static class RowToEventProcessFunction extends ProcessFunction<Row, Event> {

    @Override
    public void processElement(Row value, Context ctx, Collector<Event> out) throws Exception {
      var details =
          EmergencyStopCount.newBuilder()
              .setGeocell((long) value.getField(0))
              .setCount((int) (long) value.getField(1))
              .setAverageSpeedDiff((float) value.getField(2))
              .setMaxDeceleration((float) value.getField(3))
              .build();
      out.collect(output(details, ((LocalDateTime) value.getField(4)).toInstant(ZoneOffset.UTC)));
      System.out.println(details);
    }
  }
}
