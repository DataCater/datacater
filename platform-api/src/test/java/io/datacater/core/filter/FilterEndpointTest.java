package io.datacater.core.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.lifecycle.FiltersRepository;
import io.datacater.core.lifecycle.ImportHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(FilterEndpoint.class)
class FilterEndpointTest {
  private String filterSourceURL;
  private List<File> fileList;
  private FiltersRepository fr = new FiltersRepository();
  private ObjectMapper mapper =
      new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  @BeforeAll
  void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
    filterSourceURL =
        ClassLoader.getSystemClassLoader().getResource("lifecycleTestFolders/filters").getPath();
    fileList = ImportHelper.listFiles(filterSourceURL, ImportHelper.ImportItem.FILTER);

    Field field = FiltersRepository.class.getDeclaredField("filters");
    field.setAccessible(true);
    field.set(fr, new ArrayList<>());

    Method method =
        FiltersRepository.class.getDeclaredMethod("mapFilter", ObjectMapper.class, File.class);
    method.setAccessible(true);

    fileList.forEach(
        x -> {
          try {
            method.invoke(fr, mapper, x);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test()
  void getFilters() {
    // check for empty and contain filter
    String response = given().get().then().extract().body().asString();
    Assertions.assertEquals(true, response.contains("contain"));
    Assertions.assertEquals(true, response.contains("empty"));
  }

  @Test
  void getFilter() {
    given().when().get("/empty").then().assertThat().body("key", equalTo("empty"));
  }
}
