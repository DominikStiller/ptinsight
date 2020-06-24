package com.dxc.ptinsight;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Class to convert between protobuf Timestamp and Java Instant
 *
 * <p>Protobuf uses a count of seconds and fraction of seconds at nanosecond resolution
 */
public class Timestamps {

  public static final ZoneId TIMEZONE_HELSINKI = ZoneId.of("Europe/Helsinki");

  public static Timestamp fromInstant(Instant ts) {
    return Timestamp.newBuilder().setSeconds(ts.getEpochSecond()).setNanos(ts.getNano()).build();
  }

  public static Instant toInstant(Timestamp ts) {
    return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
  }
}
