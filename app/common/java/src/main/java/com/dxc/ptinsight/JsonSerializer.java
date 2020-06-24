package com.dxc.ptinsight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSerializer {

  private static final Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);

  private static ObjectMapper mapper;

  public static ObjectMapper getMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();

      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    return mapper;
  }

  public static Map<String, Object> parseIntoMap(String json) {
    try {
      return getMapper().readValue(json, new TypeReference<HashMap<String, Object>>() {});
    } catch (JsonProcessingException e) {
      LOG.error("Could not parse JSON", e);
      return null;
    }
  }
}
