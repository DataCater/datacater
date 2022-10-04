package io.datacater.core.transform;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.lifecycle.ImportHelper;
import io.datacater.core.lifecycle.TransformsRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(TransformEndpoint.class)
class TransformEndpointTest {
  private static final Logger logger = Logger.getLogger(TransformEndpointTest.class);
  private String transformSourceURL;
  private List<File> fileList;
  private TransformsRepository tr = new TransformsRepository();
  private ObjectMapper mapper =
      new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  @BeforeAll
  void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
    transformSourceURL =
        ClassLoader.getSystemClassLoader().getResource("lifecycleTestFolders/transforms").getPath();
    fileList = ImportHelper.listFiles(transformSourceURL, ImportHelper.ImportItem.TRANSFORM);

    Field field = TransformsRepository.class.getDeclaredField("transforms");
    field.setAccessible(true);
    field.set(tr, new ArrayList<>());

    Method method =
        TransformsRepository.class.getDeclaredMethod(
            "mapTransform", ObjectMapper.class, File.class);
    method.setAccessible(true);

    fileList.forEach(
        x -> {
          try {
            method.invoke(tr, mapper, x);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  void testGetTransforms() {
    // check for add and add-column transform
    String response = given().get().then().extract().body().asString();
    Assertions.assertEquals(true, response.contains("add"));
    Assertions.assertEquals(true, response.contains("add-column"));
  }

  @Test
  void testGetTransform() {
    given().when().get("/add").then().assertThat().body("key", equalTo("add"));
  }
}
