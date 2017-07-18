package com.youtube.gaming.mightybot.properties;

import java.util.List;

import com.google.api.client.util.Preconditions;

public class MightyModuleProperties {
  private String prefix;
  private MightyProperties properties;

  public MightyModuleProperties(String prefix, MightyProperties properties) {
    this.prefix = Preconditions.checkNotNull(prefix);
    this.properties = Preconditions.checkNotNull(properties);
  }

  public String get(MightyProperty property) {
    return properties.get(property);
  }

  public String get(String property) {
    return properties.get(prefix(property));
  }

  public List<String> getByPrefix(String prefix) {
    return properties.getByPrefix(prefix(prefix));
  }

  public int getInt(String property) {
    return properties.getInt(prefix(property));
  }

  public void throwIfNoneByPrefix(String prefix) {
    properties.throwIfNoneByPrefix(prefix(prefix));
  }

  public void throwIfNullOrEmpty(String property, String userVisibleMessage) {
    properties.throwIfNullOrEmpty(prefix(property), userVisibleMessage);
  }

  public String prefix(String property) {
    return prefix + "." + property;
  }
}
