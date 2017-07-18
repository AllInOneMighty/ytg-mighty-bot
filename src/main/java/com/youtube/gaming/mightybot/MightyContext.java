package com.youtube.gaming.mightybot;

import java.time.Clock;

import com.google.api.services.youtube.YouTube;
import com.google.common.base.Preconditions;

public class MightyContext {
  private YouTube youTube;
  private Clock clock;

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
