package com.dxc.ptinsight.processing.jobs;

import static org.apache.flink.table.api.Expressions.$;

import com.dxc.ptinsight.Resources;
import com.dxc.ptinsight.processing.flink.Job;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.Base.Event;
import com.dxc.ptinsight.proto.Base.VehicleType;
import com.dxc.ptinsight.proto.egress.Patterns.EmergencyStop;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects emergency stops by checking if a vehicle decelerates from over 10 m/s to less than 1 m/s
 * within 10 seconds
 */
public class EmergencyStopDetectionTableJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(EmergencyStopDetectionTableJob.class);

  public EmergencyStopDetectionTableJob() {
    super("Emergency Stop Detection Table");
  }

  @Override
  protected void setup() throws IOException {
    var tableEnv = getTableEnvironment();

    // Set up tables from stream
    var vehiclePositionStream =
        source("ingress.vehicle-position", VehiclePosition.class)
            .map(new VehiclePositionTableTupleBuilderProcessFunction());
    tableEnv.createTemporaryView(
        "vehicle_position",
        tableEnv.fromDataStream(
            vehiclePositionStream,
            $("vehicle_id"),
            $("vehicle_type"),
            $("lat"),
            $("lon"),
            $("speed"),
            $("acceleration"),
            $("event_time").rowtime()));

    // Detect emergency stops into table
    var emergencyStopTable =
        tableEnv.sqlQuery(Resources.getContents("sql/detectEmergencyStop.sql"));

    // Convert result table back to stream
    tableEnv
        .toAppendStream(emergencyStopTable, Row.class)
        .process(new OutputProcessFunction())
        .addSink(sink("egress.emergency-stop-table"));
  }

  private static class VehiclePositionTableTupleBuilderProcessFunction
      extends RichMapFunction<
          VehiclePosition, Tuple6<Long, VehicleType, Float, Float, Float, Float>> {

    private transient UniqueVehicleIdKeySelector<VehiclePosition> vehicleIdSelector;

    @Override
    public void open(Configuration parameters) {
      vehicleIdSelector = UniqueVehicleIdKeySelector.ofVehiclePosition();
    }

    @Override
    public Tuple6<Long, VehicleType, Float, Float, Float, Float> map(VehiclePosition value)
        throws Exception {
      return Tuple6.of(
          vehicleIdSelector.getKey(value),
          value.getVehicle().getType(),
          value.getLatitude(),
          value.getLongitude(),
          value.getSpeed(),
          value.getAcceleration());
    }
  }

  private static class OutputProcessFunction extends ProcessFunction<Row, Event> {

    @Override
    public void processElement(Row value, Context ctx, Collector<Event> out) {
      var timestamp = ((LocalDateTime) value.getField(0)).toInstant(ZoneOffset.UTC);
      var details =
          EmergencyStop.newBuilder()
              .setLatitude((float) value.getField(1))
              .setLongitude((float) value.getField(2))
              .setSpeedDiff((float) value.getField(3))
              .setMaxDeceleration((float) value.getField(4))
              .setVehicleType((VehicleType) value.getField(5))
              .build();
      out.collect(output(details, timestamp));
    }
  }
}
