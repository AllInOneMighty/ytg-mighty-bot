package com.youtube.gaming.mightybot.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Outputs the concurrent viewers and likes of the most recent live broadcast in specified files on
 * the computer.
 */
public class MostRecentLiveBroadcastConcurrentViewersAndLikes extends Module {
  private static final Logger logger = LoggerFactory.getLogger(MostRecentLiveBroadcastConcurrentViewersAndLikes.class);

  private static final String CONCURRENT_VIEWERS_OUTPUT_FILE = "concurrentViewers.outputFile";
  private static final String LIKES_OUTPUT_FILE = "likes.outputFile";
  private static final String INTERVAL = "interval";
  private static final int MINIMUM_INTERVAL = 5;

  private Path concurrentViewersOutputPath;
  private Path likesOutputPath;

  @Override
  public void checkProperties() throws InvalidConfigurationException {
    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(), CONCURRENT_VIEWERS_OUTPUT_FILE,
        Optional.of("concurrent viewers for most recent live broadcast"));
    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(), LIKES_OUTPUT_FILE,
        Optional.of("likes for most recent live broadcast"));

    getProperties().throwIfNullOrEmpty(INTERVAL, "Interval can't be empty");
    if (getProperties().getInt(INTERVAL) < MINIMUM_INTERVAL) {
      throw new InvalidConfigurationException(getProperties().addPrefix(INTERVAL),
          "Interval can't be less than 5s");
    }
  }

  @Override
  public void init() {
    logger.info("Watching concurrent viewers and likes of most recent live broadcast on channel {}",
        getProperties().get(MightyProperty.CHANNEL_ID));

    concurrentViewersOutputPath = Paths.get(getProperties().get(CONCURRENT_VIEWERS_OUTPUT_FILE));
    likesOutputPath = Paths.get(getProperties().get(LIKES_OUTPUT_FILE));
    logger.info("Writing concurrent viewers count for most recent live broadcast to file: {}",
        concurrentViewersOutputPath.toAbsolutePath().toString());
    logger.info("Writing concurrent likes count for most recent live broadcast to file: {}",
        likesOutputPath.toAbsolutePath().toString());
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
    Optional<LiveBroadcast> mostRecentLiveBroadcast =
        context.youTubeHelper().getMostRecentLiveBroadcast();
    if (mostRecentLiveBroadcast.isPresent()) {
      retrieveAndWriteConcurrentViewersAndLikes(context.youTube(),
          mostRecentLiveBroadcast.get().getId(), likesOutputPath, concurrentViewersOutputPath);
    } else {
      // No broadcast, so we just write 0 everywhere
      writeConcurrentViewersAndLikes(BigInteger.ZERO, likesOutputPath, BigInteger.ZERO,
          concurrentViewersOutputPath);
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

}
