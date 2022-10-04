package io.datacater.core.lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportHelperTest {
  private String transformSourceURL;
  private String filterSourceURL;

  @BeforeAll
  void setUp() {
    transformSourceURL =
        ClassLoader.getSystemClassLoader().getResource("lifecycleTestFolders/transforms").getPath();
    filterSourceURL =
        ClassLoader.getSystemClassLoader().getResource("lifecycleTestFolders/filters").getPath();
  }

  @Test
  void testListFilesWithNullDirectory() {
    List<File> fileList =
        ImportHelper.listFiles(
            "lifecycleTestFolders/transforms/NonExistentDirectory",
            ImportHelper.ImportItem.TRANSFORM);
    Assertions.assertEquals(Collections.EMPTY_LIST, fileList);
  }

  @Test
  void testListFilesWithEmptyDirectory() {
    List<File> fileList = ImportHelper.listFiles(null, ImportHelper.ImportItem.TRANSFORM);
    Assertions.assertEquals(Collections.EMPTY_LIST, fileList);
  }

  @ParameterizedTest
  @ValueSource(strings = {"noTransformPythonFile"})
  void testListFilesTransforms(String directoryName) {
    String url =
        ClassLoader.getSystemClassLoader()
            .getResource("lifecycleTestFolders/" + directoryName)
            .getPath();
    List<File> fileList = ImportHelper.listFiles(url, ImportHelper.ImportItem.TRANSFORM);
    Assertions.assertEquals(true, fileList.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"noFilterPythonFile"})
  void testListFilesFilters(String directoryName) {
    String url =
        ClassLoader.getSystemClassLoader()
            .getResource("lifecycleTestFolders/" + directoryName)
            .getPath();
    List<File> fileList = ImportHelper.listFiles(url, ImportHelper.ImportItem.FILTER);
    Assertions.assertEquals(true, fileList.isEmpty());
  }

  @Test
  void testListFilesWithFilters() {
    List<File> fileList = ImportHelper.listFiles(filterSourceURL, ImportHelper.ImportItem.FILTER);
    Assertions.assertEquals(2, fileList.size());
  }

  @Test
  void testListFilesWithTransforms() {
    List<File> fileList =
        ImportHelper.listFiles(transformSourceURL, ImportHelper.ImportItem.TRANSFORM);
    Assertions.assertEquals(2, fileList.size());
  }
}
