package com.dxc.ptinsight;

import com.uber.h3core.H3Core;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geocells {

  private static final Logger LOG = LoggerFactory.getLogger(Geocells.class);
  private static transient H3Core h3;

  static {
    try {
      h3 = H3Core.newInstance();
    } catch (IOException e) {
      LOG.error("Could not create H3 instance", e);
    }
  }

  public static H3Core h3() {
    return h3;
  }
}
