package com.youtube.gaming.mightybot;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.CaseFormat;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.properties.MightyModuleProperties;
import com.youtube.gaming.mightybot.properties.MightyProperties;

public abstract class Module {
  private static final String ENABLED_PROPERTY = "enabled";

  private MightyModuleProperties properties;
  private long lastRunEpochSecond;

  public String getName() {
    return this.getClass().getSimpleName();
  }

  final void setProperties(MightyProperties properties) {
    String lowerCamelName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getName());
    this.properties = new MightyModuleProperties(lowerCamelName, properties);
  }

  public MightyModuleProperties getProperties() {
    return properties;
  }

  public String getEnabledProperty() {
    return properties.prefix(ENABLED_PROPERTY);
  }

  public boolean isEnabled() {
    return "true".equalsIgnoreCase(properties.get(ENABLED_PROPERTY));
  }

  public abstract void checkProperties() throws InvalidConfigurationException;

  public abstract void init();

  @Nullable public abstract Set<String> getRequiredOauthScopes();

  public abstract long getIntervalSecond();

  public abstract void run(MightyContext context) throws Exception;

  final void setLastRunEpochSecond(long epochSecond) {
    this.lastRunEpochSecond = epochSecond;
  }

  public final long getLastRunEpochSecond() {
    return lastRunEpochSecond;
  }
}
