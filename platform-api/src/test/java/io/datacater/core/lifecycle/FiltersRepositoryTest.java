package io.datacater.core.lifecycle;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FiltersRepositoryTest {
  private static final Logger logger = Logger.getLogger(FiltersRepositoryTest.class);
  private String filterSourceURL;
  private List<File> fileList;
  private FiltersRepository fr = new FiltersRepository();
  private ObjectMapper mapper =
      new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  @BeforeAll
  void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
    filterSourceURL =
        ClassLoader.getSystemClassLoader()
            .getResource("lifecycleTestFolders/filtersRepoTests")
            .getPath();
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

  @Test
  void testAddFilterNoKey() {
    // assert no key
    Assertions.assertEquals(
        true,
        Optional.ofNullable(
                FiltersRepository.getFilters().stream()
                    .filter(x -> x.name().equals("no-key"))
                    .findFirst())
            .orElse(null)
            .isEmpty(),
        "no-key filter test failed");
  }

  @Test
  void testAddFilterOnlyKey() {
    // assert only key
    Assertions.assertEquals(
        true, FiltersRepository.getFilter("only-key").isEmpty(), "only-key filter test failed");
  }

  @Test
  void testAddFilterEmptyKey() {
    // assert empty key
    Assertions.assertEquals(
        true,
        Optional.ofNullable(
                FiltersRepository.getFilters().stream()
                    .filter(x -> x.name().equals("empty-key"))
                    .findFirst())
            .orElse(null)
            .isEmpty(),
        "empty-key filter test failed");
  }

  @Test
  void testAddFilterDuplicateEntry() {
    // assert duplicate
    Assertions.assertEquals(
        1,
        FiltersRepository.getFilters().stream().filter(x -> x.key().equals("duplicate")).count(),
        "duplicate filter test failed");
  }

  @Test
  void testAddFilterNoPythonFile() {
    // assert no py file
    Assertions.assertEquals(
        true, FiltersRepository.getFilter("no-py-file").isEmpty(), "no-py-file filter test failed");
  }

  @Test
  void testAddFilterTypesAsArray() {
    // assert types-array
    Assertions.assertEquals(
        true,
        FiltersRepository.getFilter("types-array").isPresent(),
        "types-array filter test failed");
  }

  @Test
  void testAddFilterTypesAsSingleEntry() {
    // assert types-single
    Assertions.assertEquals(
        true,
        FiltersRepository.getFilter("types-single").isPresent(),
        "types-single filter test failed");
  }

  @Test
  void testAddFilterLabelNoKey() {
    // assert labels-no-key
    Assertions.assertEquals(
        true,
        FiltersRepository.getFilter("labels-no-key").isEmpty(),
        "labels-no-key filter test failed");
  }
}
