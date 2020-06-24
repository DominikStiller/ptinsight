package com.dxc.ptinsight;

import java.io.IOException;
import java.io.InputStream;

public class Resources {
  public static InputStream getStream(String path) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  }

  public static String getContents(String path) throws IOException {
    return new String(getStream(path).readAllBytes());
  }
}
