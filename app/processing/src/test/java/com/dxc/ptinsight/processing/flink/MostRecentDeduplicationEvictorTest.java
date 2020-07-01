package com.dxc.ptinsight.processing.flink;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.flink.streaming.api.windowing.evictors.Evictor.EvictorContext;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MostRecentDeduplicationEvictorTest {

  @Test
  void shouldOnlyKeepMostRecentElement() {
    var elements = new ArrayList<TimestampedValue<String>>(5);
    elements.add(new TimestampedValue<>("foo", 500));
    elements.add(new TimestampedValue<>("bar", 1000));
    elements.add(new TimestampedValue<>("foo", 1500));
    elements.add(new TimestampedValue<>("foobar", 2000));
    elements.add(new TimestampedValue<>("bar", 2500));

    var expected = Arrays.asList(elements.get(2), elements.get(3), elements.get(4));

    var evictor = new MostRecentDeduplicationEvictor<String, String, TimeWindow>(v -> v);
    evictor.evictBefore(
        elements, elements.size(), new TimeWindow(0, 3000), Mockito.mock(EvictorContext.class));

    assertIterableEquals(expected, elements);
  }

  @Test
  void shouldKeepElementsWithKeyException() {
    var elements = new ArrayList<TimestampedValue<String>>(5);
    elements.add(new TimestampedValue<>("foo", 500));
    elements.add(new TimestampedValue<>("bar", 1000));
    elements.add(new TimestampedValue<>("foo", 1500));
    elements.add(new TimestampedValue<>("foobar", 2000));
    elements.add(new TimestampedValue<>("bar", 2500));

    var expected = List.copyOf(elements);

    var evictor =
        new MostRecentDeduplicationEvictor<String, String, TimeWindow>(
            v -> {
              throw new Exception();
            });
    evictor.evictBefore(
        elements, elements.size(), new TimeWindow(0, 3000), Mockito.mock(EvictorContext.class));

    assertIterableEquals(expected, elements);
  }

  @Test
  void shouldNotEvictAfterWindowing() {
    var elements = new ArrayList<TimestampedValue<String>>(5);
    elements.add(new TimestampedValue<>("foo", 500));
    elements.add(new TimestampedValue<>("bar", 1000));
    elements.add(new TimestampedValue<>("foo", 1500));
    elements.add(new TimestampedValue<>("foobar", 2000));
    elements.add(new TimestampedValue<>("bar", 2500));

    // elements needs to be mutable, otherwise an immutable copy is created
    // Use a copy of the TimestampedValue instances to ensure equality
    var expected = List.copyOf(elements);

    var evictor = new MostRecentDeduplicationEvictor<String, String, TimeWindow>(v -> v);
    evictor.evictAfter(
        elements, elements.size(), new TimeWindow(0, 3000), Mockito.mock(EvictorContext.class));

    // evictAfter should do nothing
    assertIterableEquals(expected, elements);
  }
}
