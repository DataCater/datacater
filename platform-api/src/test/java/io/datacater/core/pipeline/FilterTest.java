package io.datacater.core.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilterTest {
  Pipeline pipeline;

  @BeforeAll
  void setUp() throws IOException {
    URL errorLogs = ClassLoader.getSystemClassLoader().getResource("pipeline-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    pipeline = mapper.readValue(errorLogs, Pipeline.class);
  }

  @Test
  void testSerializeFilter() throws JsonProcessingException {
    // arrange
    String expected =
        "{\"attributeName\":\"age\",\"filter\":\"less-than\",\"filterConfig\":{\"value\":50}}";
    PipelineSpec spec = pipeline.getSpec();
    List<Filter> filters = spec.getFilters();
    Filter filter = filters.get(0);

    // act
    String serializedFilter =
        Filter.serializeFilter(filter.attributeName(), filter.filter(), filter.filterConfig());

    // assert
    Assertions.assertEquals(expected, serializedFilter);
  }
}
