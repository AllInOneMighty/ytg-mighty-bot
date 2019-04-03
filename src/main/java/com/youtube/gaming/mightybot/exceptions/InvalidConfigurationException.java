package com.youtube.gaming.mightybot.exceptions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.Strings;

/** Thrown when an invalid configuration is detected. */
public class InvalidConfigurationException extends RuntimeException {
  private static final long serialVersionUID = -3423776764695705850L;

  /** Details about the invalid configuration. */
  private String details;

  /**
   * Creates a new invalid configuration exception with a simple information message.
   */
  public InvalidConfigurationException(String message) {
    super(message);
  }

  /**
   * Creates a new invalid configuration exception with the name of the field that is invalid and a
   * detailed message.
   *
   * @param field the name of the field that is invalid
   * @param details the detailed message
   */
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
