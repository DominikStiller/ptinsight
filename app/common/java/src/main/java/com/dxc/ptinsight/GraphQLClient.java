package com.dxc.ptinsight;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GraphQLClient {

  private transient HttpClient client = HttpClient.newHttpClient();

  public CompletableFuture<Map<String, Object>> get(
      String endpoint, String queryPath, Map<String, String> data) throws IOException {
    var query = Resources.getContents("graphql/" + queryPath + ".graphql");
    for (var entry : data.entrySet()) {
      query = query.replace("$" + entry.getKey().toUpperCase(), entry.getValue());
    }

    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .POST(BodyPublishers.ofString(query))
            .header("Content-Type", "application/graphql")
            .build();

    return client
        .sendAsync(request, BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenApply(JsonSerializer::parseIntoMap)
        .thenApply(map -> (Map<String, Object>) map.get("data"));
  }

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    var start = System.currentTimeMillis();
    new GraphQLClient()
        .get(
            "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql",
            "fuzzytrip",
            Map.of("route", "1097", "direction", "0", "date", "2020-06-26", "time", "70560"))
        .thenAccept(
            data -> {
              System.out.println(data);
              var fuzzyTrip = (Map<String, Object>) data.get("fuzzyTrip");
              var stops = (List<Map<String, Double>>) fuzzyTrip.get("stops");
              var lastStop = stops.get(stops.size() - 1);
              var geocell = Geocells.h3().geoToH3(lastStop.get("lat"), lastStop.get("lon"), 8);
              System.out.println(geocell);
            })
        .get();
    System.out.println((System.currentTimeMillis() - start));
  }
}
