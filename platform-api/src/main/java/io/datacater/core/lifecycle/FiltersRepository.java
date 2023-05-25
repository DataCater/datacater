package io.datacater.core.lifecycle;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.filter.FilterSpec;
import io.quarkus.runtime.StartupEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class FiltersRepository {
  private static final Logger LOGGER = Logger.getLogger(FiltersRepository.class);

  private static List<FilterSpec> filters = new ArrayList<>();

  @ConfigProperty(name = "datacater.filters.path")
  private String filterSourceURL;

  private void loadFilters(@Observes StartupEvent event) {
    List<File> fileList = ImportHelper.listFiles(filterSourceURL, ImportHelper.ImportItem.FILTER);
    ObjectMapper mapper =
        new YAMLMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    fileList.forEach(x -> mapFilter(mapper, x));
  }

  private void mapFilter(ObjectMapper mapper, File file) {
    try {
      FilterSpec filter = mapper.readValue(file.getAbsoluteFile(), FilterSpec.class);
      if (isValidFilter(filter, file.getAbsoluteFile().toString())) {
        addFilter(filter);
      }
    } catch (DatabindException e) {
      LOGGER.error("incorrect filter syntax in: " + file.getAbsolutePath());
      LOGGER.error(e);
    } catch (StreamReadException e) {
      LOGGER.error("file does not contain valid yaml format: " + file.getAbsolutePath());
      LOGGER.error(e);
    } catch (IOException e) {
      LOGGER.error("file path invalid or file could not be found: " + file.getAbsolutePath());
      LOGGER.error(e);
    }
  }

  private boolean isValidFilter(FilterSpec filter, String pathToFilter) {
    if (filter.key() == null || filter.key().isEmpty()) {
      LOGGER.warn(pathToFilter + " has an empty key");
      return false;
    }
    if (getFilter(filter.key()).isPresent()) {
      LOGGER.warn(pathToFilter + " has already been added");
      return false;
    }
    return true;
  }

  public static Optional<FilterSpec> getFilter(String key) {
    return filters.stream().filter(t -> t.key().equals(key)).findFirst();
  }

  public static List<FilterSpec> getFilters() {
    return filters;
  }

  public void addFilter(FilterSpec filter) {
    filters.add(filter);
  }
}
