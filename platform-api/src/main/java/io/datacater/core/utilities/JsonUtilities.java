package io.datacater.core.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.DatacaterException;
import java.util.Map;

public class JsonUtilities {

  private JsonUtilities() {}

  @ExcludeFromGeneratedCoverageReport
  public static JsonNode convertMap(Map<String, String> map) {
    try {
      ObjectMapper om = new ObjectMapper();
      return om.reader().readTree(om.writeValueAsString(map));
    } catch (JsonProcessingException e) {
      throw new DatacaterException(e.getMessage());
    }
  }

  @ExcludeFromGeneratedCoverageReport
  public static Map<String, String> toMap(JsonNode json) {
    ObjectMapper om = new ObjectMapper();
    return om.convertValue(json, new TypeReference<>() {});
  }
}
