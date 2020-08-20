package com.dxc.ptinsight.processing.jobs;

import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.processing.flink.UniqueVehicleIdKeySelector;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TestHarnessUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlowDirectionJobTest {

  @BeforeEach
  void setUp() {
    EntryPoint.getConfiguration().h3.resolution = 5;
  }

  @Test
  void shouldDetectChangeToNeighboringCell() throws Exception {
    // Set up test environment
    var harness = createHarness();
    harness.open();

    // Run tests
    harness.processElement(createVehiclePosition(40.689167f, -74.044444f), 0);
    harness.processElement(createVehiclePosition(40.847068f, -74.201888f), 1);
    harness.processElement(createVehiclePosition(40.847078f, -74.201898f), 2);
    harness.processElement(createVehiclePosition(40.840823f, -74.411837f), 3);
    harness.close();

    // Two cell changes should be detected
    var expectedOutput = new ConcurrentLinkedQueue<>();
    expectedOutput.add(new StreamRecord<>(Tuple2.of(599718752904282111L, 599718741093122047L), 1));
    expectedOutput.add(new StreamRecord<>(Tuple2.of(599718741093122047L, 599718743240605695L), 3));

    TestHarnessUtil.assertOutputEquals("", expectedOutput, harness.getOutput());
  }

  @Test
  void shouldNotDetectChangeToNonNeighboringCell() throws Exception {
    // Set up test environment
    var harness = createHarness();
    harness.open();

    // Run tests
    harness.processElement(createVehiclePosition(40.689167f, -74.044444f), 0);
    harness.processElement(createVehiclePosition(40.840823f, -74.411837f), 1);
    harness.close();

    var expectedOutput = new ConcurrentLinkedQueue<>();
    TestHarnessUtil.assertOutputEquals("", expectedOutput, harness.getOutput());
  }

  @Test
  void shouldRestoreState() throws Exception {
    var snapshotHarness = createHarness();
    snapshotHarness.open();
    snapshotHarness.processElement(createVehiclePosition(40.689167f, -74.044444f), 0);
    var snapshot = snapshotHarness.snapshot(1, 0);
    snapshotHarness.close();

    var restoreHarness = createHarness();
    restoreHarness.initializeState(snapshot);
    restoreHarness.open();
    restoreHarness.processElement(createVehiclePosition(40.847068f, -74.201888f), 1);
    restoreHarness.close();

    // Cell change should be detected in restored harness
    var expectedOutput = new ConcurrentLinkedQueue<>();
    expectedOutput.add(new StreamRecord<>(Tuple2.of(599718752904282111L, 599718741093122047L), 1));

    TestHarnessUtil.assertOutputEquals("", expectedOutput, restoreHarness.getOutput());
  }

  private VehiclePosition createVehiclePosition(float lat, float lon) {
    return VehiclePosition.newBuilder().setLatitude(lat).setLongitude(lon).build();
  }

  private KeyedOneInputStreamOperatorTestHarness<Long, VehiclePosition, Tuple2<Long, Long>>
      createHarness() throws Exception {
    return new KeyedOneInputStreamOperatorTestHarness<>(
        new KeyedProcessOperator<>(new FlowDirectionJob.CellChangeDetectionProcessFunction()),
        UniqueVehicleIdKeySelector.ofVehiclePosition(),
        BasicTypeInfo.LONG_TYPE_INFO,
        1,
        1,
        0);
  }
}
