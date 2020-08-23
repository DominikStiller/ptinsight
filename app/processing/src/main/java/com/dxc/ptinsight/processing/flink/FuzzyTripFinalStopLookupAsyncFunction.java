package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.GraphQLClient;
import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.proto.input.HslRealtime.VehiclePosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.shaded.guava18.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava18.com.google.common.cache.CacheBuilder;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load the final stop of a route in a vehicle position message from the HSL routing API
 *
 * <p>A fuzzy trip allows retrieving trip information without the trip id but other trip information
 */
public class FuzzyTripFinalStopLookupAsyncFunction
    extends RichAsyncFunction<TimestampedValue<VehiclePosition>, Tuple2<Double, Double>> {

  private static final Logger LOG =
      LoggerFactory.getLogger(FuzzyTripFinalStopLookupAsyncFunction.class);

  private transient GraphQLClient client;
  private transient Cache<String, Optional<Tuple2<Double, Double>>> cache;

  private transient long currentCacheSize = 0;
  private transient Counter cacheHits;
  private transient Counter cacheMisses;

  @Override
  public void open(Configuration parameters) {
    client = new GraphQLClient();
    cache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.MINUTES)
            // There are usually about 1000 unique routes at any time
            // Latency markers inflate the cache since they all have different keys
            .maximumSize(10000)
            .removalListener(x -> currentCacheSize = cache.size())
            .build();

    getRuntimeContext().getMetricGroup().gauge("cacheSize", () -> currentCacheSize);
    this.cacheHits = getRuntimeContext().getMetricGroup().counter("cacheHits");
    getRuntimeContext().getMetricGroup().meter("cacheHitsPerSecond", new MeterView(this.cacheHits));
    this.cacheMisses = getRuntimeContext().getMetricGroup().counter("cacheMisses");
    getRuntimeContext()
        .getMetricGroup()
        .meter("cacheMissesPerSecond", new MeterView(this.cacheMisses));
  }

  @Override
  public void asyncInvoke(
      TimestampedValue<VehiclePosition> input, ResultFuture<Tuple2<Double, Double>> resultFuture)
      throws Exception {
    var timestamp = Instant.ofEpochMilli(input.getTimestamp()).atZone(Timestamps.TIMEZONE_HELSINKI);
    var route = input.getValue().getRoute();
    var operatingDay = LocalDate.parse(route.getOperatingDay());
    var departureTime = LocalTime.parse(route.getDepartureTime());

    // The time needs to be transformed to seconds for the request
    // https://digitransit.fi/en/developers/apis/1-routing-api/routes/#a-namefuzzytripaquery-a-trip-without-its-id
    var seconds = departureTime.toSecondOfDay();
    // The timestamp comes from the window end instead of from the actual event, since vehicle
    // positions in the final stop job are windowed first and deduplicated before lookup
    // Therefore it can be off by at most the window period (5 s)
    // This might lead to incorrect results around the departure time, but the likelihood is small
    if (!timestamp.toLocalDate().equals(operatingDay)
        && departureTime.isBefore(timestamp.toLocalTime())) {
      seconds += Duration.ofHours(24).toSeconds();
    }

    var routeId = route.getId();
    var direction = route.getDirection() ? "1" : "0";

    // Do not use RouteInfo directly since protobuf hashcode includes other fields as well
    var cacheKey = routeId + direction + operatingDay + seconds;
    var cachedGeocell = cache.getIfPresent(cacheKey);
    // Null means that this route has not yet been seen
    // Empty optional means that no fuzzy trip was found
    if (cachedGeocell != null) {
      complete(resultFuture, cachedGeocell.orElse(null));
      this.cacheHits.inc();
      return;
    }
    this.cacheMisses.inc();

    // Use direct executor only for callbacks, not for async request itself, otherwise all requests
    // will be executed on a single thread
    // https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/asyncio.html#implementation-tips
    client
        .get(
            "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql",
            "fuzzytrip",
            Map.of(
                "route",
                routeId,
                "direction",
                direction,
                "date",
                operatingDay.toString(),
                "time",
                String.valueOf(seconds)))
        .thenAccept(
            data -> {
              try {
                var fuzzyTrip = (Map<String, Object>) data.get("fuzzyTrip");
                if (fuzzyTrip == null) {
                  complete(resultFuture, null);
                  cache.put(cacheKey, Optional.empty());
                  return;
                }

                var stops = (List<Map<String, Double>>) fuzzyTrip.get("stops");
                var lastStop = stops.get(stops.size() - 1);
                var coords = Tuple2.of(lastStop.get("lat"), lastStop.get("lon"));
                complete(resultFuture, coords);
                cache.put(cacheKey, Optional.of(coords));
                currentCacheSize = cache.size();
              } catch (Exception e) {
                LOG.error("Could not extract coordinates from final stop", e);
                complete(resultFuture, null);
              }
            });
  }

  @Override
  public void timeout(
      TimestampedValue<VehiclePosition> input, ResultFuture<Tuple2<Double, Double>> resultFuture) {
    resultFuture.complete(Collections.emptyList());
  }

  private void complete(
      ResultFuture<Tuple2<Double, Double>> resultFuture, Tuple2<Double, Double> value) {
    if (value == null) {
      resultFuture.complete(Collections.emptyList());
    } else {
      resultFuture.complete(Collections.singleton(value));
    }
  }
}
