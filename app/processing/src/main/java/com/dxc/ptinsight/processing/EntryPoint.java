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
      try {
        configuration =
            YamlSerializer.getMapper()
                .readValue(
                    Configuration.class.getClassLoader().getResourceAsStream("processing.yaml"),
                    Configuration.class);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return configuration;
  }
}
