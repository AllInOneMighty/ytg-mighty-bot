package com.youtube.gaming.mightybot;

import java.time.Clock;

import com.google.api.services.youtube.YouTube;
import com.google.common.base.Preconditions;

/**
 * The mighty context in which modules are ran. Provides the {@link YouTube} API and the clock to
 * the bot modules.
 */
public class MightyContext {
  private YouTube youTube;
  private Clock clock;

  /**
   * Creates a new mighty context with the given {@link YouTube} API and clock. The API needs to be
   * already connected when this method is called.
   *
   * @param youTube a connected {@link YouTube} API
   * @param clock the system clock used by the bot
   */
  MightyContext(YouTube youTube, Clock clock) {
    this.youTube = Preconditions.checkNotNull(youTube);
    this.clock = Preconditions.checkNotNull(clock);
  }

  public YouTube youTube() {
    return youTube;
  }

  public Clock clock() {
    return clock;
  }
}
