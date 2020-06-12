package com.dxc.ptinsight;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonSerializer {

  private static ObjectMapper mapper;

  public static ObjectMapper getMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();

      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    return mapper;
  }
}
