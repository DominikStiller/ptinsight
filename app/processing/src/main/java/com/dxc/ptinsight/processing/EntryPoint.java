package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.Resources;
import com.dxc.ptinsight.YamlSerializer;
import com.dxc.ptinsight.processing.jobs.DelayDetectionJob;
import com.dxc.ptinsight.processing.jobs.FinalStopCountJob;
import com.dxc.ptinsight.processing.jobs.FlowDirectionJob;
import com.dxc.ptinsight.processing.jobs.VehicleCountJob;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryPoint {

  private static final Logger LOG = LoggerFactory.getLogger(EntryPoint.class);

  public static Configuration configuration;

  public static void main(String[] args) throws Exception {
    new VehicleCountJob().execute();
    new DelayDetectionJob().execute();
    new FlowDirectionJob().execute();
    new FinalStopCountJob().execute();
  }

  public static Configuration getConfiguration() {
    if (configuration == null) {
      var stream = Resources.getStream("processing.yaml");
      if (stream == null) {
        stream = Resources.getStream("processing.default.yaml");
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
