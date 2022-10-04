package io.datacater.core.lifecycle;

import java.io.File;
import java.util.*;
import org.jboss.logging.Logger;

public class ImportHelper {
  private static final Logger LOGGER = Logger.getLogger(ImportHelper.class);
  private static final String SPEC_YML = "spec.yml";
  private static final String SPEC_YAML = "spec.yaml";
  private static final String PY_EXTENSION = ".py";

  public enum ImportItem {
    FILTER("filter"),
    TRANSFORM("transform");

    private final String name;

    ImportItem(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public String getFileName() {
      return name + PY_EXTENSION;
    }
  }

  public static List<File> listFiles(final String directory, ImportItem importItem) {
    if (directory == null) {
      LOGGER.warn("no directory found to be imported");
      return Collections.emptyList();
    }
    List<File> fileList = new ArrayList<>();
    File[] files = new File(directory).listFiles();

    if (files == null) {
      LOGGER.warn("no " + importItem + " found to be imported");
      return Collections.emptyList();
    }

    for (File element : files) {
      if (element.isDirectory()) {
        fileList.addAll(listFiles(element.getPath(), importItem));
      } else {
        Optional<File> pythonFile =
            Arrays.stream(files)
                .filter(x -> x.getName().equals(importItem.getFileName()))
                .findFirst();
        if ((element.getName().equals(SPEC_YML) || element.getName().equals(SPEC_YAML))
            && pythonFile.isPresent()) {
          fileList.add(element);
        }
      }
    }
    return fileList;
  }
}
