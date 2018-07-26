package com.youtube.gaming.mightybot.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.util.ModuleUtils;

/**
 * Outputs the most recent live broadcast title to a file.
 */
public class CurrentLiveBroadcastTitle extends Module {
  private static final Logger logger = LoggerFactory.getLogger(CurrentLiveBroadcastTitle.class);

  private static final String CURRENT_LIVE_BROADCAST_TITLE_OUTPUT_FILE =
      "currentLiveBroadcastTitle.outputFile";
  private static final String INTERVAL = "interval";
  private static final int MINIMUM_INTERVAL = 15;

  private Path currentVideoTitleOutputPath;

  @Override
  public void checkProperties() throws InvalidConfigurationException {
    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(),
        CURRENT_LIVE_BROADCAST_TITLE_OUTPUT_FILE, Optional.of("title for current live broadcast"));

    getProperties().throwIfNullOrEmpty(INTERVAL, "Interval can't be empty");
    if (getProperties().getInt(INTERVAL) < MINIMUM_INTERVAL) {
      throw new InvalidConfigurationException(getProperties().addPrefix(INTERVAL),
          "Interval can't be less than 15s");
    }
  }

  @Override
  public void init() {
    currentVideoTitleOutputPath =
        Paths.get(getProperties().get(CURRENT_LIVE_BROADCAST_TITLE_OUTPUT_FILE));
    logger.info("Writing title of current live broadcast to file: {}",
        currentVideoTitleOutputPath.toAbsolutePath().toString());
  }

  @Override
  @Nullable
  public Set<String> getRequiredOauthScopes() {
    return ImmutableSet.of("https://www.googleapis.com/auth/youtube");
  }

  @Override
  public long getIntervalSecond() {
    return getProperties().getInt(INTERVAL);
  }

  @Override
  public void run(MightyContext context) throws Exception {
    LiveBroadcast mostRecentLiveBroadcast = getMostRecentLiveBroadcast(context);
    if (mostRecentLiveBroadcast == null) {
      logger.info("No live broadcast found (an active broadcast isn't necessarily live).");
      return;
    }

    try (BufferedWriter writer = Files.newBufferedWriter(currentVideoTitleOutputPath)) {
      writer.write(mostRecentLiveBroadcast.getSnippet().getTitle());
    } catch (FileSystemException e) {
      logger.warn("Output writing of current live broadcast title failed. Skipping...", e);
    }
  }

  private LiveBroadcast getMostRecentLiveBroadcast(MightyContext context) throws IOException {
    LiveBroadcast mostRecentLiveBroadcast =
        getMostRecentLiveBroadcast(null, context.youTubeHelper().getActiveBroadcasts());
    return getMostRecentLiveBroadcast(mostRecentLiveBroadcast,
        context.youTubeHelper().getActivePersistentBroadcasts());
  }

  private LiveBroadcast getMostRecentLiveBroadcast(LiveBroadcast broadcast,
      List<LiveBroadcast> activeBroadcasts) {
    for (LiveBroadcast activeBroadcast : activeBroadcasts) {
      if (!activeBroadcast.getStatus().getLifeCycleStatus().equals("live")) {
        continue;
      }
      if (broadcast == null || broadcast.getSnippet().getActualStartTime()
          .getValue() < activeBroadcast.getSnippet().getActualStartTime().getValue()) {
        broadcast = activeBroadcast;
      }
    }
    return broadcast;
  }

}
