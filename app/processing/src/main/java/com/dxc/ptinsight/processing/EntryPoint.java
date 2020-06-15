package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.YamlSerializer;
import com.dxc.ptinsight.processing.jobs.VehicleCountJob;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryPoint {

  private static final Logger LOG = LoggerFactory.getLogger(EntryPoint.class);

  public static Configuration configuration;

  public static void main(String[] args) throws Exception {
    new VehicleCountJob().execute();
  }

  public static Configuration getConfiguration() {
    if (configuration == null) {
      var stream =
          Thread.currentThread().getContextClassLoader().getResourceAsStream("processing.yaml");
      if (stream == null) {
        stream =
            Configuration.class.getClassLoader().getResourceAsStream("processing.default.yaml");
      }
      try {
        configuration = YamlSerializer.getMapper().readValue(stream, Configuration.class);
      } catch (IOException e) {
        LOG.error("Could not read config file", e);
      }
    }
    return configuration;
  }
}
