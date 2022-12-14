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
public class ConfigTest {
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

    Assertions.assertEquals(config.name(), "config1");
    Assertions.assertEquals(config.kind(), Kind.STREAM);
    Assertions.assertEquals(spec.get("prop1"), "asdf");
    Assertions.assertEquals(spec.get("prop2"), "qwert");
  }
}
