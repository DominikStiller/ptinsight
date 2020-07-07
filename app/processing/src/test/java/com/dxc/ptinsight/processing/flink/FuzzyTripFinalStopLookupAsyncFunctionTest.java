package com.dxc.ptinsight.processing.flink;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.dxc.ptinsight.GraphQLClient;
import com.dxc.ptinsight.processing.EntryPoint;
import com.dxc.ptinsight.proto.ingress.HslRealtime.RouteInfo;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream.OutputMode;
import org.apache.flink.streaming.api.functions.async.AsyncFunction;
import org.apache.flink.streaming.api.operators.async.AsyncWaitOperatorFactory;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TestHarnessUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

// Uses examples from official API description
// https://digitransit.fi/en/developers/apis/1-routing-api/routes/#a-namefuzzytripaquery-a-trip-without-its-id
class FuzzyTripFinalStopLookupAsyncFunctionTest {

  private static final int TIMEOUT = 2000;

  @BeforeEach
  void setUp() {
    EntryPoint.getConfiguration().h3.resolution = 5;
  }

  @Test
  void shouldGetCorrectFinalStopButIgnoreTimeouts() throws Exception {
    // Set up mock API responses
    var mockGraphQLClient = Mockito.mock(GraphQLClient.class);
    doReturn(
            CompletableFuture.supplyAsync(
                () -> {
                  try {
                    Thread.sleep(TIMEOUT * 10);
                  } catch (InterruptedException e) {
                  }
                  return getNullFuzzyTrip();
                }))
        .when(mockGraphQLClient)
        .get(anyString(), anyString(), argThat(data -> data.get("route").equals("1550")));
    doReturn(CompletableFuture.completedFuture(getRealFuzzyTrip()))
        .when(mockGraphQLClient)
        .get(anyString(), anyString(), argThat(data -> data.get("route").equals("2550")));

    // Build test messages
    var timestamp1 = Instant.parse("2019-06-28T08:49:01.457Z").toEpochMilli();
    var vehiclePosition1 = createVehiclePosition("1550", "2019-06-28", "10:57");

    var timestamp2 = Instant.parse("2019-06-28T09:49:01.457Z").toEpochMilli();
    var vehiclePosition2 = createVehiclePosition("2550", "2019-06-28", "11:57");

    // Set up test environment
    var function = new FuzzyTripFinalStopLookupAsyncFunction();
    var harness = createHarness(function);
    harness.open();

    FieldSetter.setField(
        function, function.getClass().getDeclaredField("client"), mockGraphQLClient);

    // Run test
    var expectedOutput = new ConcurrentLinkedQueue<>();
    // Should timeout
    harness.processElement(new TimestampedValue<>(vehiclePosition1, timestamp1), timestamp1);

    harness.setProcessingTime(TIMEOUT / 2);
    // Should be processed normally
    harness.processElement(new TimestampedValue<>(vehiclePosition2, timestamp2), timestamp2);
    expectedOutput.add(new StreamRecord<>(Tuple2.of(42.0, 42.0), timestamp2));

    harness.setProcessingTime(TIMEOUT + 1);
    harness.close();

    // Check output
    TestHarnessUtil.assertOutputEquals(
        "No timed-out record should be present", expectedOutput, harness.getOutput());
    verify(mockGraphQLClient)
        .get(anyString(), anyString(), argThat(data -> data.get("time").equals("43020")));
  }

  @Test
  void shouldDismissRecordsWithNoFuzzyTripMatches() throws Exception {
    // Set up mock API responses
    var mockGraphQLClient = Mockito.mock(GraphQLClient.class);
    when(mockGraphQLClient.get(anyString(), anyString(), anyMap()))
        .thenReturn(CompletableFuture.completedFuture(getNullFuzzyTrip()));

    // Build test messages
    var timestamp = Instant.parse("2019-06-28T08:49:01.457Z").toEpochMilli();
    var vehiclePosition = createVehiclePosition("1550", "2019-06-28", "10:57");

    // Set up test environment
    var function = new FuzzyTripFinalStopLookupAsyncFunction();
    var harness = createHarness(function);
    harness.open();

    FieldSetter.setField(
        function, function.getClass().getDeclaredField("client"), mockGraphQLClient);

    // Run test
    // Should find no fuzzy trip
    harness.processElement(new TimestampedValue<>(vehiclePosition, timestamp), timestamp);
    harness.close();

    // Check output
    TestHarnessUtil.assertOutputEquals(
        "No record should be present", new ConcurrentLinkedQueue<>(), harness.getOutput());
  }

  @Test
  void shouldUseCorrectDepartureTimeWhenCurrentAndOperatingDayDiffer() throws Exception {
    // Set up mock API response
    var mockGraphQLClient = Mockito.mock(GraphQLClient.class);
    when(mockGraphQLClient.get(anyString(), anyString(), anyMap()))
        .thenReturn(CompletableFuture.completedFuture(getNullFuzzyTrip()));

    // Build test message where timestamp day and operating day are different
    var timestamp = Instant.parse("2018-08-16T00:15:00.836Z").toEpochMilli();
    var vehiclePosition = createVehiclePosition("2550", "2018-08-15", "03:10");

    // Set up test environment
    var function = new FuzzyTripFinalStopLookupAsyncFunction();
    var harness = createHarness(function);
    harness.open();

    FieldSetter.setField(
        function, function.getClass().getDeclaredField("client"), mockGraphQLClient);

    // Run test
    harness.processElement(new TimestampedValue<>(vehiclePosition, timestamp), timestamp);
    harness.close();

    // Check if 24 h in seconds were added to the "time" field
    verify(mockGraphQLClient)
        .get(anyString(), anyString(), argThat(data -> data.get("time").equals("97800")));
  }

  @Test
  void shouldUseCachedValue() throws Exception {
    // Set up mock API responses
    var mockGraphQLClient = Mockito.mock(GraphQLClient.class);
    when(mockGraphQLClient.get(anyString(), anyString(), anyMap()))
        .thenReturn(CompletableFuture.completedFuture(getRealFuzzyTrip()))
        .thenReturn(CompletableFuture.completedFuture(getNullFuzzyTrip()));

    // Build test messages
    var timestamp1 = Instant.parse("2019-06-28T09:49:01.457Z").toEpochMilli();
    var timestamp2 = Instant.parse("2019-06-28T09:49:02.457Z").toEpochMilli();
    var vehiclePosition = createVehiclePosition("1550", "2019-06-28", "10:57");

    // Set up test environment
    var function = new FuzzyTripFinalStopLookupAsyncFunction();
    var harness = createHarness(function);
    harness.open();

    FieldSetter.setField(
        function, function.getClass().getDeclaredField("client"), mockGraphQLClient);

    // Run test
    var expectedOutput = new ConcurrentLinkedQueue<>();
    // Should find fuzzy trip
    harness.processElement(new TimestampedValue<>(vehiclePosition, timestamp1), timestamp1);
    // Should find no fuzzy trip but use cached result
    harness.processElement(new TimestampedValue<>(vehiclePosition, timestamp2), timestamp2);
    expectedOutput.add(new StreamRecord<>(Tuple2.of(42.0, 42.0), timestamp1));
    expectedOutput.add(new StreamRecord<>(Tuple2.of(42.0, 42.0), timestamp2));

    harness.close();

    // Check output
    TestHarnessUtil.assertOutputEquals(
        "Should use cached value", expectedOutput, harness.getOutput());
  }

  private VehiclePosition createVehiclePosition(
      String route, String operatingDay, String departureTime) {
    return VehiclePosition.newBuilder()
        .setRoute(
            RouteInfo.newBuilder()
                .setId(route)
                .setOperatingDay(operatingDay)
                .setDepartureTime(departureTime)
                .build())
        .build();
  }

  private Map<String, Object> getRealFuzzyTrip() {
    return Map.of(
        "fuzzyTrip",
        Map.of(
            "stops", List.of(Map.of("lat", 41.0, "lon", 41.0), Map.of("lat", 42.0, "lon", 42.0))));
  }

  private Map<String, Object> getNullFuzzyTrip() {
    // Map.of does not allow null values
    var nullFuzzyTrip = new HashMap<String, Object>();
    nullFuzzyTrip.put("fuzzyTrip", null);
    return nullFuzzyTrip;
  }

  private OneInputStreamOperatorTestHarness<
          TimestampedValue<VehiclePosition>, Tuple2<Double, Double>>
      createHarness(
          AsyncFunction<TimestampedValue<VehiclePosition>, Tuple2<Double, Double>> function)
          throws Exception {
    var asyncOperator = new AsyncWaitOperatorFactory<>(function, TIMEOUT, 2, OutputMode.ORDERED);

    return new OneInputStreamOperatorTestHarness<>(
        asyncOperator,
        TypeInformation.of(new TypeHint<TimestampedValue<VehiclePosition>>() {})
            .createSerializer(new ExecutionConfig()));
  }
}
