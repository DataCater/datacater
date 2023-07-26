package io.datacater.core.project;

import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class ProjectUtilities {
  private ProjectUtilities() {}

  public static Uni<List<ProjectEntity>> getMappedProjects(
      Map<String, String> projects, Mutiny.Session session) {

    if (projects == null) {
      return Uni.createFrom().item(new ArrayList<>());
    }

    return session
        .createQuery("from ProjectEntity", ProjectEntity.class)
        .getResultList()
        .onItem()
        .transform(
            projectEntityList ->
                projectEntityList.stream()
                    .filter(
                        item ->
                            stringMapsContainsEqualKey(
                                JsonUtilities.toStringMap(
                                    item.getMetadata().get(StaticConfig.LABELS)),
                                projects))
                    .toList())
        .onItem()
        .ifNull()
        .continueWith(new ArrayList<>());
  }

  private static boolean stringMapsContainsEqualKey(
      Map<String, String> givenMap, Map<String, String> currentMap) {
    if (givenMap == null || currentMap == null || givenMap.isEmpty() || currentMap.isEmpty()) {
      return false;
    }
    for (Map.Entry<String, String> givenEntry : givenMap.entrySet()) {
      if (currentMap.containsKey(givenEntry.getKey())
          && Objects.equals(givenEntry.getValue(), currentMap.get(givenEntry.getKey()))) {
        return true;
      }
    }
    return false;
  }
}
