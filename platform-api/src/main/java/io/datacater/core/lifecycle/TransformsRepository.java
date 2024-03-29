package io.datacater.core.lifecycle;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.transform.TransformSpec;
import io.quarkus.runtime.StartupEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class TransformsRepository {
  private static final Logger LOGGER = Logger.getLogger(TransformsRepository.class);

  private static List<TransformSpec> transforms = new ArrayList<>();

  @ConfigProperty(name = "datacater.transforms.path")
  private String transformSourceURL;

  @ExcludeFromGeneratedCoverageReport
  private void loadTransforms(@Observes StartupEvent event) {
    List<File> fileList =
        ImportHelper.listFiles(transformSourceURL, ImportHelper.ImportItem.TRANSFORM);
    ObjectMapper mapper =
        new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    fileList.forEach(x -> mapTransform(mapper, x));
  }

  // TODO rework logs to one line
  private void mapTransform(ObjectMapper mapper, File file) {
    try {
      TransformSpec transform = mapper.readValue(file.getAbsoluteFile(), TransformSpec.class);
      if (isValidTransform(transform, file.getAbsoluteFile().toString())) {
        addTransform(transform);
      }
    } catch (DatabindException e) {
      LOGGER.error(
          String.format(
              StaticConfig.LoggerMessages.INCORRECT_TRANSFORM_SYNTAX,
              file.getAbsolutePath(),
              System.lineSeparator(),
              e));
    } catch (StreamReadException e) {
      LOGGER.error(
          String.format(
              StaticConfig.LoggerMessages.FILE_INVALID_YML,
              file.getAbsolutePath(),
              System.lineSeparator(),
              e));
    } catch (IOException e) {
      LOGGER.error(
          String.format(
              StaticConfig.LoggerMessages.FILE_INVALID_PATH,
              file.getAbsolutePath(),
              System.lineSeparator(),
              e));
    }
  }

  private boolean isValidTransform(TransformSpec transform, String pathToTransform) {
    if (transform.key() == null || transform.key().isEmpty()) {
      LOGGER.warn(String.format(StaticConfig.LoggerMessages.HAS_EMPTY_KEY, pathToTransform));
      return false;
    }
    if (getTransform(transform.key()).isPresent()) {
      LOGGER.warn(String.format(StaticConfig.LoggerMessages.HAS_BEEN_ADDED, pathToTransform));
      return false;
    }
    return true;
  }

  public static Optional<TransformSpec> getTransform(String key) {
    return transforms.stream().filter(t -> t.key().equals(key)).findFirst();
  }

  public static List<TransformSpec> getTransforms() {
    return transforms;
  }

  public void addTransform(TransformSpec transform) {
    transforms.add(transform);
  }
}
