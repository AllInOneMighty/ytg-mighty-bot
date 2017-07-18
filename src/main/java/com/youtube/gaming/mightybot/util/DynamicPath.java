package com.youtube.gaming.mightybot.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DynamicPath {
  public static Path locate(String fileName) {
    // First try to locate from JAR
    URL propertiesUrl = ClassLoader.getSystemResource(fileName);
    if (propertiesUrl != null) {
      try {
        return Paths.get(propertiesUrl.toURI());
      } catch (URISyntaxException e) {
        throw new AssertionError("Hardcoded system resource name is invalid. Are you drunk?");
      }
    }

    // If not found, locate on file system
    return Paths.get("./" + fileName);
  }
}
