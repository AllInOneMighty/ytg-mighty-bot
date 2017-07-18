package com.youtube.gaming.mightybot;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.api.services.youtube.YouTube;
import com.google.common.base.CaseFormat;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.properties.MightyModuleProperties;
import com.youtube.gaming.mightybot.properties.MightyProperties;

/** A mighty bot module. **/
public abstract class Module {
  private static final String ENABLED_PROPERTY = "enabled";

  private MightyModuleProperties properties;
  private long lastRunEpochSecond;

  /** Returns the name of this module. Should not be overridden for most cases. */
  public String getName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Initializes the {@link MightyModuleProperties} of this module with the given
   * {@link MightyProperties} so they can be easily accessed with {@link #getProperties()}.
   */
  final void setProperties(MightyProperties properties) {
    String lowerCamelName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getName());
    this.properties = new MightyModuleProperties(lowerCamelName, properties);
  }

  /** Returns the {@link MightyModuleProperties} of this module. */
  public MightyModuleProperties getProperties() {
    return properties;
  }

  /** Returns the enabled property of this module, which is {@code "moduleName.enabled"}. */
  public String getEnabledProperty() {
    return properties.addPrefix(ENABLED_PROPERTY);
  }

  /** Returns {@code true} if this module is enabled in the configuration of the bot. */
  public boolean isEnabled() {
    return "true".equalsIgnoreCase(properties.get(ENABLED_PROPERTY));
  }

  /**
   * Checks if the properties of this module are present and enabled. This method should make an
   * extensive use of the {@link MightyModuleProperties#throwIfNullOrEmpty(String, String)} (use
   * {@link MightyModuleProperties#throwIfNoneByPrefix(String)} for properties that can have
   * multiple values).
   *
   * <p>Checks that should be done by this method include but are not limited to: checking that a date
   * is correctly formatted, a value is in an acceptable range, a file path exists/is readable/is
   * writable, etc.
   *
   * @throws InvalidConfigurationException if any property is missing or invalid
   * @throws RuntimeException if any other error occurs when validating the properties (I/O, ...)
   */
  public abstract void checkProperties() throws InvalidConfigurationException;

  /**
   * Initializes the module.
   *
   * <p>
   * Examples:
   * <ul>
   * <li>Read the properties and save them in private variables.
   * <li>Create {@link Path} objects for files that should be read or written by the module when
   * running
   * <li>Read module configuration from files.
   * <li>...
   * </ul>
   */
  public abstract void init();

  /** Returns a set of OAuth scopes that this module requires when using the {@link YouTube} API. */
  @Nullable public abstract Set<String> getRequiredOauthScopes();

  /** Returns the number of seconds to wait between each run of the module. */
  public abstract long getIntervalSecond();

  /**
   * The meaty part of the module, where the task is executed. This is only called after
   * {@link #getIntervalSecond()} has passed since the last time this module ran.
   *
   * <p>
   * This method should generally not raise an exception when something bad happens, but rather log
   * the error and return successfully. In the case a critical error occurs, it is okay to throw an
   * exception as it will be caught by the main loop and logged.
   *
   * <p>
   * Examples:
   * <ul>
   * <li>Requests information from the YouTube API.
   * <li>Writes updated information to files on disk.
   * <li>Writes a message to chat using the YouTube API.
   * <li>...
   * </ul>
   *
   * @param context the context in which the task is be executed
   *
   * @throws Exception if an unrecoverable error occurs
   */
  public abstract void run(MightyContext context) throws Exception;

  /**
   * Sets at which time this module has last run. Called immediately after the
   * {@link #run(MightyContext)} is complete.
   */
  final void setLastRunEpochSecond(long epochSecond) {
    this.lastRunEpochSecond = epochSecond;
  }

  /**
   * Returns the time at which this module last ran. If the module has never run, returns 0.
   */
  public final long getLastRunEpochSecond() {
    return lastRunEpochSecond;
  }
}
