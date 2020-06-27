package com.dxc.ptinsight.processing.flink;

import com.dxc.ptinsight.GraphQL;
import com.dxc.ptinsight.Timestamps;
import com.dxc.ptinsight.proto.ingress.HslRealtime.VehiclePosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.shaded.guava18.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava18.com.google.common.cache.CacheBuilder;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuzzyTripFinalStopLookupAsyncFunction
    extends RichAsyncFunction<Tuple2<Instant, VehiclePosition>, Tuple2<VehiclePosition, Long>> {

  private static final Logger LOG =
      LoggerFactory.getLogger(FuzzyTripFinalStopLookupAsyncFunction.class);

  private transient GeocellKeySelector<Tuple2<Double, Double>> cellSelector;
  private transient Cache<String, Long> cache;

  private transient long currentCacheSize = 0;
  private transient Counter cacheHits;
  private transient Counter cacheMisses;

  @Override
  public void open(Configuration parameters) {
    cellSelector = GeocellKeySelector.ofTuple2();
    cache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1500)
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
      Tuple2<Instant, VehiclePosition> input,
      ResultFuture<Tuple2<VehiclePosition, Long>> resultFuture)
      throws Exception {
    var timestamp = input.f0.atZone(Timestamps.TIMEZONE_HELSINKI);
    var operatingDay = LocalDate.parse(input.f1.getRoute().getOperatingDay());
    var departureTime = LocalTime.parse(input.f1.getRoute().getDepartureTime());

    // The time needs to be transformed to seconds for the request
    // https://digitransit.fi/en/developers/apis/1-routing-api/routes/#a-namefuzzytripaquery-a-trip-without-its-id
    var seconds = departureTime.toSecondOfDay();
    if (!timestamp.toLocalDate().equals(operatingDay)
        && departureTime.isBefore(timestamp.toLocalTime())) {
      seconds += Duration.ofHours(24).toSeconds();
    }

    var route = input.f1.getRoute().getId();
    var direction = input.f1.getRoute().getDirection() ? "1" : "0";

    // Do not use RouteInfo directly since protobuf hashcode includes other fields as well
    var cacheKey = route + direction + operatingDay + seconds;
    var cached = cache.getIfPresent(cacheKey);
    if (cached != null) {
      resultFuture.complete(Collections.singleton(Tuple2.of(input.f1, cached)));
      this.cacheHits.inc();
      return;
    }
    this.cacheMisses.inc();

    // Use direct executor only for callbacks, not for async request itself, otherwise all requests
    // will be executed on a single thread
    // https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/asyncio.html#implementation-tips
    GraphQL.get(
            "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql",
            "fuzzytrip",
            Map.of(
                "route",
                route,
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
                var stops = (List<Map<String, Double>>) fuzzyTrip.get("stops");
                var lastStop = stops.get(stops.size() - 1);
                var geocell =
                    cellSelector.getKey(Tuple2.of(lastStop.get("lat"), lastStop.get("lon")));
                resultFuture.complete(Collections.singleton(Tuple2.of(input.f1, geocell)));
                cache.put(cacheKey, geocell);
                currentCacheSize = cache.size();
              } catch (Exception e) {
                LOG.error("Could not extract geocell from final stop", e);
                resultFuture.complete(Collections.emptyList());
              }
            });
  }

  @Override
  public void timeout(
      Tuple2<Instant, VehiclePosition> input,
      ResultFuture<Tuple2<VehiclePosition, Long>> resultFuture) {
    resultFuture.complete(Collections.emptyList());
  }
}
