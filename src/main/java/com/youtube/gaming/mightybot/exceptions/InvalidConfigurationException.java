package com.youtube.gaming.mightybot.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.Strings;

public class InvalidConfigurationException extends RuntimeException {
  private String details;

  public InvalidConfigurationException(String message) {
    super(message);
  }

  public InvalidConfigurationException(String field, String details) {
    this(String.format("Incorrect value for field: '%s'", field));
    this.details = checkNotNull(details);
  }

  @Override
  public String toString() {
    if (Strings.isNullOrEmpty(details)) {
      return super.toString();
    } else {
      return String.format("%s - %s", super.getMessage(), details);
    }
  }
}
