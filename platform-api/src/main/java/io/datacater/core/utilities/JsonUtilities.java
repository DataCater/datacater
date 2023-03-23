package io.datacater.core.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.DatacaterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtilities {

  private JsonUtilities() {}

  @ExcludeFromGeneratedCoverageReport
  public static JsonNode convertStringListMap(Map<String, List<String>> map) {
    try {
      ObjectMapper om = new ObjectMapper();
      return om.reader().readTree(om.writeValueAsString(map));
    } catch (JsonProcessingException e) {
      throw new DatacaterException(e.getMessage());
    }
  }

  @ExcludeFromGeneratedCoverageReport
  public static JsonNode convertStringMap(Map<String, String> map) {
    try {
      ObjectMapper om = new ObjectMapper();
      return om.reader().readTree(om.writeValueAsString(map));
    } catch (JsonProcessingException e) {
      throw new DatacaterException(e.getMessage());
    }
  }

  @ExcludeFromGeneratedCoverageReport
  public static Map<String, List<String>> toStringListMap(JsonNode json) {
    ObjectMapper om = new ObjectMapper();
    Map<String, List<String>> map = om.convertValue(json, new TypeReference<>() {});
    if (map == null) {
      return new HashMap<>();
    }
    return map;
  }

  @ExcludeFromGeneratedCoverageReport
  public static Map<String, String> toStringMap(JsonNode json) {
    ObjectMapper om = new ObjectMapper();
    Map<String, String> map = om.convertValue(json, new TypeReference<>() {});
    if (map == null) {
      return new HashMap<>();
    }
    return map;
  }

  @ExcludeFromGeneratedCoverageReport
  public static Map<String, Object> toObjectMap(JsonNode json) {
    ObjectMapper om = new ObjectMapper();
    Map<String, Object> map = om.convertValue(json, new TypeReference<>() {});
    if (map == null) {
      return new HashMap<>();
    }
    return map;
  }

  @ExcludeFromGeneratedCoverageReport
  public static Map<String, Object> combineMaps(
      Map<String, Object> prio2, Map<String, Object> prio1) {
    Map<String, Object> result = new HashMap<>(prio2);
    for (Map.Entry<String, Object> entry : prio1.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (result.containsKey(key)) {
        Object existingValue = result.get(key);
        if (existingValue instanceof Map && value instanceof Map) {
          result.put(
              key, combineMaps((Map<String, Object>) existingValue, (Map<String, Object>) value));
        } else {
          result.put(key, value);
        }
      } else {
        result.put(key, value);
      }
    }
    return result;
  }
}
