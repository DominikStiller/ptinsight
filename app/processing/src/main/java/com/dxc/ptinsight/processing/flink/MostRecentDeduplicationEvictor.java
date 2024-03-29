package com.dxc.ptinsight.processing.flink;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Evict all but the most recent record by key */
public class MostRecentDeduplicationEvictor<T, KEY, W extends Window> implements Evictor<T, W> {

  private static final Logger LOG = LoggerFactory.getLogger(MostRecentDeduplicationEvictor.class);

  private final KeySelector<T, KEY> keySelector;

  private MostRecentDeduplicationEvictor(KeySelector<T, KEY> keySelector) {
    this.keySelector = keySelector;
  }

  @Override
  public void evictBefore(
      Iterable<TimestampedValue<T>> elements, int size, W window, EvictorContext evictorContext) {
    if (!hasTimestamp(elements)) {
      return;
    }

    var mostRecentTimestamps = new HashMap<KEY, Long>();

    // First iteration: find most recent timestamp for each key
    elements.forEach(e -> mostRecentTimestamps.put(getKey(e), e.getTimestamp()));

    // Second iteration: remove elements which are not most recent
    for (var iterator = elements.iterator(); iterator.hasNext(); ) {
      var e = iterator.next();
      var key = getKey(e);
      if (key != null && e.getTimestamp() < mostRecentTimestamps.get(key)) {
        iterator.remove();
      }
    }
  }

  @Override
  public void evictAfter(
      Iterable<TimestampedValue<T>> elements, int size, W window, EvictorContext evictorContext) {}

  private KEY getKey(TimestampedValue<T> e) {
    try {
      return keySelector.getKey(e.getValue());
    } catch (Exception exception) {
      LOG.error("Could not extract key", exception);
    }
    return null;
  }

  private boolean hasTimestamp(Iterable<TimestampedValue<T>> elements) {
    Iterator<TimestampedValue<T>> it = elements.iterator();
    if (it.hasNext()) {
      return it.next().hasTimestamp();
    }
    return false;
  }

  /** An evictor which keeps only the most recent record for each key */
  public static <T, KEY, W extends Window> MostRecentDeduplicationEvictor<T, KEY, W> of(
      KeySelector<T, KEY> keySelector) {
    return new MostRecentDeduplicationEvictor<>(keySelector);
  }

  /** An evictor which keeps only the most recent record */
  public static <T, W extends Window> MostRecentDeduplicationEvictor<T, Integer, W> ofAll() {
    return new MostRecentDeduplicationEvictor<>((value -> 0));
  }
}
