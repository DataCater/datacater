package io.datacater.core.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransformsRepositoryTest {
  private static final Logger logger = Logger.getLogger(TransformsRepositoryTest.class);
  private String transformSourceURL;
  private List<File> fileList;
  private TransformsRepository tr = new TransformsRepository();
  private ObjectMapper mapper =
      new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  @BeforeAll
  void setUp() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
    transformSourceURL =
        ClassLoader.getSystemClassLoader()
            .getResource("lifecycleTestFolders/transformsRepoTests")
            .getPath();
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
  void addTransformNoKey() {
    // assert no key
    Assertions.assertEquals(
        true,
        Optional.ofNullable(
                TransformsRepository.getTransforms().stream()
                    .filter(x -> x.name().equals("no-key"))
                    .findFirst())
            .orElse(null)
            .isEmpty(),
        "no-key transform test failed");
  }

  @Test
  void addTransformOnlyKey() {
    // assert only key
    Assertions.assertEquals(
        true,
        TransformsRepository.getTransform("only-key").isEmpty(),
        "only-key transform test failed");
  }

  @Test
  void addTransformEmptyKey() {
    // assert empty key
    Assertions.assertEquals(
        true,
        Optional.ofNullable(
                TransformsRepository.getTransforms().stream()
                    .filter(x -> x.name().equals("empty-key"))
                    .findFirst())
            .orElse(null)
            .isEmpty(),
        "empty-key transform test failed");
  }

  @Test
  void addTransformDuplicateEntries() {
    // assert duplicate
    Assertions.assertEquals(
        1,
        TransformsRepository.getTransforms().stream()
            .filter(x -> x.key().equals("duplicate"))
            .count(),
        "duplicate transform test failed");
  }

  @Test
  void addTransformNoPythonFile() {
    // assert no py file
    Assertions.assertEquals(
        true,
        TransformsRepository.getTransform("no-py-file").isEmpty(),
        "no-py-file transform test failed");
  }

  @Test
  void addTransformTypesAsArray() {
    // assert types-array
    Assertions.assertEquals(
        true,
        TransformsRepository.getTransform("types-array").isPresent(),
        "types-array transform test failed");
  }

  @Test
  void addTransformTypesAsSingleEntry() {
    // assert types-single
    Assertions.assertEquals(
        true,
        TransformsRepository.getTransform("types-single").isPresent(),
        "types-single transform test failed");
  }

  @Test
  void addTransformLabelNoKey() {
    // assert labels-no-key
    Assertions.assertEquals(
        true,
        TransformsRepository.getTransform("labels-no-key").isEmpty(),
        "labels-no-key transform test failed");
  }
}
