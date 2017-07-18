package com.youtube.gaming.mightybot.properties;

public enum MightyProperty {
  PROJECT_ID("projectId"),
  CHANNEL_ID("channelId"),
  API_KEY("apiKey");

  private String name;

  MightyProperty(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }
}
