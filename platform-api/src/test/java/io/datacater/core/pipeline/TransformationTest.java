package io.datacater.core.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransformationTest {
  Pipeline pipeline;

  @BeforeAll
  void setUp() throws IOException {
    URL errorLogs = ClassLoader.getSystemClassLoader().getResource("pipeline-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    pipeline = mapper.readValue(errorLogs, Pipeline.class);
  }

  @Test
  void testSerializeTransformation() throws JsonProcessingException {
    // arrange
    String expected =
        "{\"attributeName\":\"email\",\"transformation\":\"hash\",\"transformationConfig\":{\"algorithm\":\"sha1\"},\"filter\":null,\"filterConfig\":null}";
    PipelineSpec spec = pipeline.getSpec();
    TransformationStep transformationStep = spec.getTransformationSteps().get(0);
    Transformation transformation = transformationStep.getTransformations().get(0);

    // act
    String serializedTransformation =
        Transformation.serializeTransformation(
            transformation.attributeName(),
            transformation.transformation(),
            transformation.transformationConfig(),
            transformation.filter(),
            transformation.filterConfig());

    // assert
    Assertions.assertEquals(expected, serializedTransformation);
  }
}
