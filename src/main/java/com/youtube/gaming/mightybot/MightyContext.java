package com.youtube.gaming.mightybot;

import java.time.Clock;

import com.google.api.services.youtube.YouTube;
import com.google.common.base.Preconditions;
import com.youtube.gaming.mightybot.properties.MightyProperties;

/**
 * The mighty context in which modules are ran. Provides the {@link YouTube} API and the clock to
 * the bot modules.
 */
public class MightyContext {
  private final YouTube youTube;
  private final YouTubeHelper youTubeHelper;
  private final Clock clock;

  /**
   * Creates a new mighty context with the given {@link YouTube} API and clock. The API needs to be
   * already connected when this method is called.
   *
   * @param youTube a connected {@link YouTube} API
   * @param clock the system clock used by the bot
   */
  MightyContext(MightyProperties properties, YouTube youTube, Clock clock) {
    this.youTube = Preconditions.checkNotNull(youTube);
    this.youTubeHelper = new YouTubeHelper(properties, youTube, clock);
    this.clock = Preconditions.checkNotNull(clock);
  }

  public YouTube youTube() {
    return youTube;
  }

  public YouTubeHelper youTubeHelper() {
    return youTubeHelper;
  }

  public Clock clock() {
    return clock;
  }
}
