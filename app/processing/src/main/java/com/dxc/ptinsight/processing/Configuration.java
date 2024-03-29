package com.dxc.ptinsight.processing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Configuration {
  public KafkaConfiguration kafka;
  public H3Configuration h3;

  public static class KafkaConfiguration {
    @JsonProperty("protobuf_format")
    public String protobufFormat = "binary";

    @JsonProperty("bootstrap_servers")
    public List<String> bootstrapServers = List.of();
  }

  public static class H3Configuration {
    public int resolution = 10;
  }
}
