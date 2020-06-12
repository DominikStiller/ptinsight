package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.YamlSerializer;
import com.dxc.ptinsight.processing.jobs.ArrivalCountJob;
import java.io.IOException;

public class EntryPoint {

  public static Configuration configuration;

  public static void main(String[] args) throws Exception {
    new ArrivalCountJob().execute();
  }

  public static Configuration getConfiguration() {
    if (configuration == null) {
      var stream = Configuration.class.getClassLoader().getResourceAsStream("processing.yaml");
      if (stream == null) {
        stream =
            Configuration.class.getClassLoader().getResourceAsStream("processing.default.yaml");
      }
      try {
        configuration = YamlSerializer.getMapper().readValue(stream, Configuration.class);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return configuration;
  }
}
