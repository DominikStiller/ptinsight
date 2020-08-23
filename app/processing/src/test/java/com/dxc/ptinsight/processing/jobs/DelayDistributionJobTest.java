package com.dxc.ptinsight.processing.jobs;

import static org.junit.jupiter.api.Assertions.*;

import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.processing.jobs.DelayDistributionJob.DelayCalculatorProcessFunction;
import com.dxc.ptinsight.proto.input.HslRealtime.Arrival;
import java.time.Instant;
import java.util.List;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.operators.ProcessOperator;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;

class DelayDistributionJobTest {

  @Test
  void shouldDetectDelays() throws Exception {
    var harness = createHarness();
    harness.open();

    harness.processElement(
        createArrival(createInstant("09:42:00")), createInstant("09:42:42").toEpochMilli());
    harness.processElement(
        createArrival(createInstant("10:42:00")), createInstant("10:45:00").toEpochMilli());
    harness.processElement(
        createArrival(createInstant("11:42:00")), createInstant("11:47:42").toEpochMilli());

    assertEquals(
        List.of(createResultTuple(0), createResultTuple(3), createResultTuple(5)),
        harness.extractOutputValues());
  }

  private Instant createInstant(String time) {
    return Instant.parse("2020-06-29T" + time + "Z");
  }

  private Tuple3<Float, Float, Long> createResultTuple(int minutes) {
    return Tuple3.of(42f, 42f, (long) minutes);
  }

  private Arrival createArrival(Instant scheduledArrival) {
    return Arrival.newBuilder()
        .setLatitude(42)
        .setLongitude(42)
        .setScheduledArrival(Timestamps.fromInstant(scheduledArrival))
        .build();
  }

  private OneInputStreamOperatorTestHarness<Arrival, Tuple3<Float, Float, Long>> createHarness()
      throws Exception {
    return new OneInputStreamOperatorTestHarness<>(
        new ProcessOperator<>(new DelayCalculatorProcessFunction()), 1, 1, 0);
  }
}
