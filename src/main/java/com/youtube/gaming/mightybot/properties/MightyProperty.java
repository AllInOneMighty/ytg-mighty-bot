package com.youtube.gaming.mightybot.properties;

/**
 * Properties that are enforced to be present by the bot and can be accessed directly by all
 * modules.
 */
public enum MightyProperty {
  /**
   * Project ID as shown on <a href="https://console.developers.google.com/apis/dashboard">Google
   * APIs Dashboard</a>.
   */
  PROJECT_ID("projectId"),
  /** Channel ID of the user, for example: {@code UCOpNcN46UbXVtpKMrmU4Abg}. */
  CHANNEL_ID("channelId"),
  /**
   * API key as shown on <a href="https://console.developers.google.com/apis/credentials">Google
   * APIs Credentials</a>.
   */
  API_KEY("apiKey"),
  /** Whether persistent broadcasts ({@code www.youtube.com/live_dashboard}) should be ignored. */
  IGNORE_PERSISTENT_BROADCASTS("ignorePersistentBroadcasts");

  private String name;

  MightyProperty(String name) {
    this.name = name;
  }

  /** Returns the name of the property as it appears in the properties file. */
  String getName() {
    return name;
  }
}
