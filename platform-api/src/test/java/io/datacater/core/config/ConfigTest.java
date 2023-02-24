package io.datacater.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.config.enums.Kind;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigTest {
  Config config;

  @BeforeAll
  void setUp() throws IOException {
    URL configJson =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/post/post-config-test1-valid.json");
    ObjectMapper objectMapper = new JsonMapper();
    config = objectMapper.readValue(configJson, Config.class);
  }

  @Test
  void test() {
    Map<String, Object> spec = config.spec();
    String expectedMetaData = "{labels={app.datacater.io/name=config1}}";

    Assertions.assertEquals("config1", config.name());
    Assertions.assertEquals(Kind.STREAM, config.kind());
    Assertions.assertEquals(expectedMetaData, config.metadata().toString());
    Assertions.assertEquals("asdf", spec.get("prop1"));
    Assertions.assertEquals("qwert", spec.get("prop2"));
  }
}
