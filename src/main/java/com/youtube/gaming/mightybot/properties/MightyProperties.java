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

/** Base class to access the bot's properties. */
public final class MightyProperties {
  private static final Logger logger = LoggerFactory.getLogger(MightyProperties.class);

  /** Name of the bot's properties file. */
  private static final String FILE_NAME = "mighty.properties";

  private Properties properties = new Properties();

  /** Retrieves a predefined bot property. */
  public String get(MightyProperty property) {
    return get(property.getName());
  }

  /** Retrieves a bot property using its canonical name. */
  public String get(String property) {
    return getProperties().getProperty(property);
  }

  /**
   * Retrieves all properties starting with a specific prefix and ending with a number that does not
   * start with {@code 0}.
   *
   * <p>
   * For example, this method would retrieve these properties:
   * <ul>
   * <li>{@code prefix3}
   * <li>{@code prefix10}
   * </ul>
   *
   * ...but not these properties:
   * <ul>
   * <li>{@code prefix}
   * <li>{@code prefix03}
   * </ul>
   */
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

  /** Retrieves a property using its canonical name and converts it to an {@code int}. */
  public int getInt(String property) {
    return Integer.parseInt(get(property).trim());
  }

  /**
   * Tries to retrieve a predefined property and raises an exception if {@code null} or empty.
   *
   * @param property the predefined property to retrieve
   * @param userVisibleMessage message to instantiate the exception with if the property is not
   *        found
   *
   * @throws InvalidConfigurationException if the property is {@code null} or empty
   */
  public void throwIfNullOrEmpty(MightyProperty property, String userVisibleMessage) {
    throwIfNullOrEmpty(property.getName(), userVisibleMessage);
  }

  /**
   * Tries to retrieve a property using its canonical name and raises an exception if {@code null}
   * or empty.
   *
   * @param property the property to retrieve
   * @param userVisibleMessage message to instantiate the exception with if the property is not
   *        found
   *
   * @throws InvalidConfigurationException if the property is {@code null} or empty
   */
  public void throwIfNullOrEmpty(String property, String userVisibleMessage) {
    if (Strings.isNullOrEmpty(get(property))) {
      throw new InvalidConfigurationException(property, userVisibleMessage);
    }
  }

  /**
   * Raises an exception if no property with the given prefix and ending with a number exists. See
   * {@link #getByPrefix(String)} for more information.
   *
   * @param prefix the prefix to search properties for
   *
   * @throws InvalidConfigurationException if no property with the given prefix is found
   */
  public void throwIfNoneByPrefix(String prefix) {
    if (getByPrefix(prefix).size() == 0) {
      throw new InvalidConfigurationException(prefix + "X",
          "You need to set at least one property named '" + prefix + "1'");
    }
  }

  /** Returns the properties from the bot's property file, initializing them if needed. */
  private Properties getProperties() {
    if (properties.isEmpty()) {
      loadProperties();
    }
    return properties;
  }

  /** Loads the properties from the bot's properties file. */
  private void loadProperties() {
    Path propertiesPath = DynamicPath.locate(FILE_NAME);

    logger.info("Reading properties from: {}", propertiesPath.toAbsolutePath());
    try (InputStream input = new FileInputStream(propertiesPath.toAbsolutePath().toFile());
        Reader reader = new InputStreamReader(input, "UTF-8")) {
      properties.load(reader);
    } catch (IOException e) {
      logger.error("Could not load properties", e);
    }
  }
}
