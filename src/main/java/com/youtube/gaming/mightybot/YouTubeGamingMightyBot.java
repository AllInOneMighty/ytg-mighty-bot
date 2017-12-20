package com.youtube.gaming.mightybot;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.ImmutableList;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.modules.ConcurrentViewsAndLikes;
import com.youtube.gaming.mightybot.modules.CurrentTime;
import com.youtube.gaming.mightybot.modules.NewSubChatAnnouncer;
import com.youtube.gaming.mightybot.modules.SubCount;
import com.youtube.gaming.mightybot.oauth.Auth;
import com.youtube.gaming.mightybot.properties.MightyProperties;
import com.youtube.gaming.mightybot.properties.MightyProperty;

/**
 * A modular and mighty YouTube Gaming bot.
 */
public class YouTubeGamingMightyBot {
  private static final Logger logger = LoggerFactory.getLogger(YouTubeGamingMightyBot.class);

  /**
   * Time to wait between each run of the main loop. This time will be added after the main loop has
   * run, which means if the main loop takes 3,000 millis to run, the next loop will run 4,000
   * millis after it originally started.
   */
  private static final long MAIN_LOOP_CYCLE_MILLIS = 1000;

  public static void main(String[] args) {
    // Get the mighty properties
    MightyProperties properties = new MightyProperties();

    // Do global configuration checks
    try {
      doGlobalConfigurationChecks(properties);
    } catch (InvalidConfigurationException e) {
      logger.error("Invalid configuration", e);
      return;
    }

    // Load all enabled modules, check configuration and initialize them
    ImmutableList<Module> modules = ImmutableList.of(
        new SubCount(),
        new CurrentTime(),
        new NewSubChatAnnouncer(),
        new ConcurrentViewsAndLikes());
    Set<String> requiredOauthScopes = new HashSet<>();
    boolean atLeastOneModuleEnabled = false;
    for (Module module : modules) {
      module.setProperties(properties);
      if (module.isEnabled()) {
        atLeastOneModuleEnabled = true;
        module.checkProperties();
        module.init();
        if (module.getRequiredOauthScopes() != null) {
          requiredOauthScopes.addAll(module.getRequiredOauthScopes());
        }
      } else {
        logger.info("{} is disabled ('{}' != true)", module.getName(), module.getEnabledProperty());
      }
    }

    if (!atLeastOneModuleEnabled) {
      logger.info("No module enabled. Please enable at least one by setting the appropriate "
          + "property to 'true'.");
      return;
    }

    // Initialize YouTube
    YouTube youTube;
    try {
      if (requiredOauthScopes.isEmpty()) {
        youTube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null)
            .setApplicationName(properties.get(MightyProperty.PROJECT_ID))
            .build();
      } else {
        Credential credential = Auth.authorize(new ArrayList<>(requiredOauthScopes), "mightybot");
        youTube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName(properties.get(MightyProperty.PROJECT_ID))
            .build();
      }
    } catch (IOException | GeneralSecurityException e) {
      logger.error("Could not initialize the YouTube API.", e);
      return;
    }

    Clock clock = Clock.systemDefaultZone();

    // Start the loop
    MightyContext context = new MightyContext(youTube, clock);
    boolean loop = true;
    while(loop) {
      for (Module module : modules) {
        if (module.isEnabled() && clock.instant().getEpochSecond()
            - module.getLastRunEpochSecond() > module.getIntervalSecond()) {
          logger.info("Running module {}", module.getName());
          try {
            module.run(context);
          } catch (Exception e) {
            logger.error("Failed to run module {}", module.getName(), e);
          }
          module.setLastRunEpochSecond(clock.instant().getEpochSecond());
        }
      }

      try {
        Thread.sleep(MAIN_LOOP_CYCLE_MILLIS);
      } catch (InterruptedException e) {
        logger.error("Could not make the thread sleep. Quitting.");
        loop = false;
      }
    }
  }

  /** Checks that the mandatory properties are present in the given properties file. */
  private static void doGlobalConfigurationChecks(MightyProperties properties) {
    for (MightyProperty property : MightyProperty.values()) {
      properties.throwIfNullOrEmpty(property, "Property can't be empty");
    }
  }
}
