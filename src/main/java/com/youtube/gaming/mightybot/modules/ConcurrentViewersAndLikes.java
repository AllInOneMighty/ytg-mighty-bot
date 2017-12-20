package com.youtube.gaming.mightybot.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Strings;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.properties.MightyProperty;
import com.youtube.gaming.mightybot.util.ModuleUtils;

/**
 * Outputs the concurrent viewers and likes of persistent and non-persistent broadcast in specified
 * files on the computer.
 */
public class ConcurrentViewersAndLikes extends Module {
  private static final Logger logger = LoggerFactory.getLogger(ConcurrentViewersAndLikes.class);

  private static final String IGNORE_PERSISTENT_BROADCASTS = "ignorePersistentBroadcasts";
  private static final String PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE =
      "persistent.concurrentViewers.outputFile";
  private static final String PERSISTENT_LIKES_OUTPUT_FILE = "persistent.likes.outputFile";
  private static final String NON_PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE =
      "nonPersistent.concurrentViewers.outputFile";
  private static final String NON_PERSISTENT_LIKES_OUTPUT_FILE = "nonPersistent.likes.outputFile";
  private static final String INTERVAL = "interval";
  private static final int MINIMUM_INTERVAL = 5;

  private Path persistentConcurrentViewersOutputPath;
  private Path persistentLikesOutputPath;
  private Path nonPersistentConcurrentViewersOutputPath;
  private Path nonPersistentLikesOutputPath;

  @Override
  public void checkProperties() throws InvalidConfigurationException {
    if (Strings.isNullOrEmpty(getProperties().get(IGNORE_PERSISTENT_BROADCASTS))) {
      logger.info(
          "You can ignore persistent broadcasts (gaming.youtube.com/channel/live/) by "
              + "setting the property {} to true",
          getProperties().addPrefix(IGNORE_PERSISTENT_BROADCASTS));
    } else if (!shouldIgnorePersistentBroadcasts()) {

    }

    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(),
        NON_PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE,
        Optional.of("concurrent viewers for non-persistent streams"));
    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(),
        NON_PERSISTENT_LIKES_OUTPUT_FILE,
        Optional.of("likes for non-persistent streams"));

    if (!shouldIgnorePersistentBroadcasts()) {
      logger.info("Will retrieve concurrent viewers and likes for persistent broadcasts.");
      ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(),
          PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE,
          Optional.of("concurrent viewers for persistent streams"));
      ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(),
          PERSISTENT_LIKES_OUTPUT_FILE,
          Optional.of("likes  for persistent streams"));
    }

    getProperties().throwIfNullOrEmpty(INTERVAL, "Interval can't be empty");
    if (getProperties().getInt(INTERVAL) < MINIMUM_INTERVAL) {
      throw new InvalidConfigurationException(getProperties().addPrefix(INTERVAL),
          "Interval can't be less than 5s");
    }
  }

  @Override
  public void init() {
    logger.info("Watching concurrent viewers and likes of channel {}",
        getProperties().get(MightyProperty.CHANNEL_ID));

    nonPersistentConcurrentViewersOutputPath =
        Paths.get(getProperties().get(NON_PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE));
    nonPersistentLikesOutputPath = Paths.get(getProperties().get(NON_PERSISTENT_LIKES_OUTPUT_FILE));
    logger.info("Writing concurrent viewers count for non-persistent broadcasts to file: {}",
        nonPersistentConcurrentViewersOutputPath.toAbsolutePath().toString());
    logger.info("Writing concurrent likes count for non-persistent broadcasts to file: {}",
        nonPersistentLikesOutputPath.toAbsolutePath().toString());

    if (!shouldIgnorePersistentBroadcasts()) {
      persistentConcurrentViewersOutputPath =
          Paths.get(getProperties().get(PERSISTENT_CONCURRENT_VIEWERS_OUTPUT_FILE));
      persistentLikesOutputPath = Paths.get(getProperties().get(PERSISTENT_LIKES_OUTPUT_FILE));
      logger.info("Writing concurrent viewers count for persistent broadcasts to file: {}",
          persistentConcurrentViewersOutputPath.toAbsolutePath().toString());
      logger.info("Writing concurrent likes count for persistent broadcasts  to file: {}",
          persistentLikesOutputPath.toAbsolutePath().toString());
    }
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
    if (!shouldIgnorePersistentBroadcasts()) {
      // Persistent broadcasts
      List<LiveBroadcast> activePersistentBroadcasts =
          context.youTubeHelper().getActivePersistentBroadcasts();
      processBroadcasts(context.youTube(), activePersistentBroadcasts, persistentLikesOutputPath,
          persistentConcurrentViewersOutputPath, "persistent");
    }

    // Non persistent broadcasts
    List<LiveBroadcast> activeBroadcasts = context.youTubeHelper().getActiveBroadcasts();
    processBroadcasts(context.youTube(), activeBroadcasts, nonPersistentLikesOutputPath,
        nonPersistentConcurrentViewersOutputPath, "non-persistent");
  }

  private void processBroadcasts(YouTube youTube, List<LiveBroadcast> broadcasts, Path likesPath,
      Path concurrentViewersPath, String broadcastType) throws IOException {
    if (broadcasts.size() > 0) {
      if (broadcasts.size() > 1) {
        logger.warn(
            "YouTube API returned more than one active %s broadcast (%s), using data "
                + "from first one: %s",
                broadcastType, broadcasts.size(), broadcasts.get(0).getId());
      }
      retrieveAndWriteConcurrentViewersAndLikes(youTube, broadcasts.get(0).getId(), likesPath,
          concurrentViewersPath);
    } else {
      // No broadcast, so we just write 0 everywhere
      writeConcurrentViewersAndLikes(BigInteger.ZERO, likesPath, BigInteger.ZERO,
          concurrentViewersPath);
    }

  }

  private void retrieveAndWriteConcurrentViewersAndLikes(YouTube youTube, String videoId,
      Path likesPath, Path concurrentViewersPath) throws IOException {
    YouTube.Videos.List request = youTube.videos().list("statistics,liveStreamingDetails");
    request.setId(videoId);

    VideoListResponse response = request.execute();
    if (response.getItems().size() != 1) {
      // Should never happen since we specified a specific video id
      throw new RuntimeException(String.format(
          "YouTube API returned more than one video for id %s: %s", videoId, response.getItems()));
    }
    Video video = response.getItems().get(0);
    BigInteger likes = video.getStatistics().getLikeCount();
    if (likes == null) {
      likes = BigInteger.ZERO;
    }
    BigInteger concurrentViewers = video.getLiveStreamingDetails().getConcurrentViewers();
    if (concurrentViewers == null) {
      concurrentViewers = BigInteger.ZERO;
    }

    // Writing to files
    writeConcurrentViewersAndLikes(likes, likesPath, concurrentViewers, concurrentViewersPath);
  }

  private void writeConcurrentViewersAndLikes(BigInteger likes, Path likesPath,
      BigInteger concurrentViewers, Path concurrentViewersPath) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(likesPath)) {
      writer.write(likes.toString());
    } catch (FileSystemException e) {
      logger.warn("Output writing of likes failed. Skipping...", e);
    }
    if (concurrentViewers != null) {
      try (BufferedWriter writer = Files.newBufferedWriter(concurrentViewersPath)) {
        writer.write(concurrentViewers.toString());
      } catch (FileSystemException e) {
        logger.warn("Output writing of concurrent viewers failed. Skipping...", e);
      }
    }
  }

  private boolean shouldIgnorePersistentBroadcasts() {
    return "true".equalsIgnoreCase(getProperties().get(IGNORE_PERSISTENT_BROADCASTS));
  }

}
