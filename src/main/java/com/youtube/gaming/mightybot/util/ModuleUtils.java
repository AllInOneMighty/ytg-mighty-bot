package com.youtube.gaming.mightybot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Optional;
import com.youtube.gaming.mightybot.properties.MightyModuleProperties;

/**
 * Utility methods for bot modules.
 */
public class ModuleUtils {

  /**
   * Verifies that the given module properties contain a non-empty value for the given file
   * property name and if so, verifies that the file exists and is writable. If the file does not
   * exist, it automatically tries to create it.
   *
   * @param properties the module properties from which to read the value of the file property name
   * @param filePropertyName the file property name to read from the module properties name
   * @param optFriendlyName a human friendly name that will be used in log messages in case an
   *     error happens (property is empty, file is not writable, ...)
   */
  public static void assertModuleFileExistsAndWriteable(MightyModuleProperties properties,
      String filePropertyName, Optional<String> optFriendlyName) {
    String friendlyName = optFriendlyName.or("output");
    String friendlyNameCapitalized =
        friendlyName.substring(0, 1).toUpperCase() + friendlyName.substring(1);
    properties.throwIfNullOrEmpty(filePropertyName,
        String.format("%s file can't be empty.", friendlyNameCapitalized));
    Path outputPath = Paths.get(properties.get(filePropertyName));
    if (!Files.exists(outputPath)) {
      try {
        Files.createFile(outputPath);
      } catch (IOException e) {
        throw new RuntimeException(
            String.format("Could not create %s file: %s", friendlyName, outputPath), e);
      }
    }
    if (!Files.isWritable(outputPath)) {
      throw new RuntimeException(String.format("%s file is not writeable: %s",
          friendlyNameCapitalized, outputPath));
    }
  }
}
