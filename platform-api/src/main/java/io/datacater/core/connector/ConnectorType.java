package io.datacater.core.connector;

import java.util.List;
import java.util.Map;

public interface ConnectorType {
  String getName();
  // @see for choice of type
  // https://github.com/apache/kafka/blob/trunk/connect/api/src/main/java/org/apache/kafka/connect/connector/Connector.java#L60
  List<Map<String, String>> getConfiguration();

  List<String> getRequiredFields();
}
