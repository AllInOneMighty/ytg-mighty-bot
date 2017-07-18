package com.youtube.gaming.mightybot.properties;

import java.util.List;

import com.google.api.client.util.Preconditions;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;

/** Gives access to the module's properties. */
public class MightyModuleProperties {
  private String prefix;
  private MightyProperties properties;

  public MightyModuleProperties(String prefix, MightyProperties properties) {
    this.prefix = Preconditions.checkNotNull(prefix);
    this.properties = Preconditions.checkNotNull(properties);
  }

  /** Retrieves a predefined global bot property. */
  public String get(MightyProperty property) {
    return properties.get(property);
  }

  /** Retrieves a module property, prefixed with the module prefix. */
  public String get(String property) {
    return properties.get(addPrefix(property));
  }

  /** Retrieves all module properties, identified with the module prefix. */
  public List<String> getByPrefix(String prefix) {
    return properties.getByPrefix(addPrefix(prefix));
  }

  /**
   * Retrieves a module property, prefixed with the module prefix, and converts it to an
   * {@code int}.
   */
  public int getInt(String property) {
    return properties.getInt(addPrefix(property));
  }

  /**
   * Raises an exception if no <i>module</i> property with the given prefix and ending with a number
   * exists. For example, if the module is named {@code myModule} and this method is called with
   * {@code "myProperty"}, it will throw an exception if no property such as
   * {@code "myModule.myProperty1"} exists in the bot configuration.
   *
   * @param prefix the prefix to search properties for
   *
   * @throws InvalidConfigurationException if no module property with the given prefix is found
   */
  public void throwIfNoneByPrefix(String prefix) {
    properties.throwIfNoneByPrefix(addPrefix(prefix));
  }

  /**
   * Tries to retrieve a module property and raises an exception if {@code null} or empty. A module
   * property is always prefixed with the module name.
   *
   * @param property the module property to retrieve
   * @param userVisibleMessage message to instantiate the exception with if the property is not
   *        found
   *
   * @throws InvalidConfigurationException if the property is {@code null} or empty
   */
  public void throwIfNullOrEmpty(String property, String userVisibleMessage) {
    properties.throwIfNullOrEmpty(addPrefix(property), userVisibleMessage);
  }

  /** Returns the given property, prefixed with the module name. */
  public String addPrefix(String property) {
    return prefix + "." + property;
  }
}
