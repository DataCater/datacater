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
  /**
   * This method combines two Map<String, Object> with each other. If a value node contains a map,
   * the `.putAll()` method would overwrite some keys. To avoid this, this method was implemented.
   * It takes a map, `lower_priority_map`, and overwrites the values/adds the keys from another map,
   * `higher_priority_map`
   *
   * @param lower_priority_map map with the lowest priority
   * @param higher_priority_map map with the highest priority. Values from this map overwrite values
   *     from lower_priority_map
   * @return a combined map
   */
  public static Map<String, Object> combineMaps(
      Map<String, Object> lower_priority_map, Map<String, Object> higher_priority_map) {
    Map<String, Object> result = new HashMap<>(lower_priority_map);
    // loop over higher_priority_map map to replace values
    for (Map.Entry<String, Object> entry : higher_priority_map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (result.containsKey(key)) {
        Object existingValue = result.get(key);
        // if the value is a map, recursively add sub-nodes so nothing is overwritten with empty
        // values
        if (existingValue instanceof Map && value instanceof Map) {
          result.put(
              key, combineMaps((Map<String, Object>) value, (Map<String, Object>) existingValue));
        } else {
          // do not overwrite value if it is empty
          if (value != "") {
            // if it is now a map, overwrite the value
            result.put(key, value);
          }
        }
      } else {
        // do not add value if it is empty
        if (value != "") {
          // add the value if it doesn't exist
          result.put(key, value);
        }
      }
    }
    return result;
  }
}
