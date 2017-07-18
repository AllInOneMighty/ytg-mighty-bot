package com.youtube.gaming.mightybot.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.util.DynamicPath;

public final class MightyProperties {
  private static final Logger logger = LoggerFactory.getLogger(MightyProperties.class);

  private static final String FILE_NAME = "mighty.properties";

  private Properties properties = new Properties();

  public String get(MightyProperty property) {
    return get(property.getName());
  }

  public String get(String property) {
    return getProperties().getProperty(property);
  }

  public List<String> getByPrefix(String prefix) {
    Pattern pattern = Pattern.compile(prefix + "[1-9][0-9]*");
    ImmutableList.Builder<String> propertiesWithPrefix = ImmutableList.builder();
    for (Entry<Object, Object> property : properties.entrySet()) {
      if (pattern.matcher(property.getKey().toString()).matches()) {
        String value = property.getValue().toString().trim();
        if (value.length() > 0) {
          propertiesWithPrefix.add(property.getValue().toString());
        }
      }
    }
    return propertiesWithPrefix.build();
  }

  public int getInt(String property) {
    return Integer.parseInt(get(property).trim());
  }

  public void throwIfNullOrEmpty(MightyProperty property, String userVisibleMessage) {
    throwIfNullOrEmpty(property.getName(), userVisibleMessage);
  }

  public void throwIfNullOrEmpty(String property, String userVisibleMessage) {
    if (Strings.isNullOrEmpty(get(property))) {
      throw new InvalidConfigurationException(property, userVisibleMessage);
    }
  }

  public void throwIfNoneByPrefix(String prefix) {
    if (getByPrefix(prefix).size() == 0) {
      throw new InvalidConfigurationException(prefix + "X",
          "You need to set at least one property named '" + prefix + "1'");
    }
  }

  private Properties getProperties() {
    if (properties.isEmpty()) {
      loadProperties();
    }
    return properties;
  }

  private void loadProperties() {
    Path propertiesPath = DynamicPath.locate(FILE_NAME);

    try (InputStream input = new FileInputStream(propertiesPath.toAbsolutePath().toFile());
        Reader reader = new InputStreamReader(input, "UTF-8")) {
      properties.load(reader);
    } catch (IOException e) {
      logger.error("Could not load properties", e);
    }
  }
}
