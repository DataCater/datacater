package io.datacater.core.utilities;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtilities {

  private StringUtilities() {}

  @ExcludeFromGeneratedCoverageReport
  public static String wrapString(String text) {
    String[] lines = text.split(System.lineSeparator());
    List<String> computed = new ArrayList<>();

    for (String s : lines) {
      String line = s;
      while (line.length() > 120) {
        computed.add(line.substring(0, 119));
        line = line.substring(120);
      }
    }

    return computed.stream().collect(Collectors.joining("\n"));
  }
}
