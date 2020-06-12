package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.YamlSerializer;
import com.dxc.ptinsight.processing.jobs.ArrivalCountJob;

public class EntryPoint {

  public static Configuration configuration;

  public static void main(String[] args) throws Exception {
    configuration =
        YamlSerializer.getMapper()
            .readValue(
                Configuration.class.getClassLoader().getResourceAsStream("processing.yaml"),
                Configuration.class);
        new ArrivalCountJob().execute();
  }

  public static Configuration getConfiguration() {
    return configuration;
  }
}
