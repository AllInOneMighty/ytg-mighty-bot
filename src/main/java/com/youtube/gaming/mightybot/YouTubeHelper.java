package com.youtube.gaming.mightybot;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.youtube.gaming.mightybot.properties.MightyProperties;
import com.youtube.gaming.mightybot.properties.MightyProperty;

public class YouTubeHelper {
  private static final Logger logger = LoggerFactory.getLogger(YouTubeHelper.class);

  private static final long ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS = 60000;
  private static final List<String> BROADCAST_ACTIVE_LIFE_CYCLES =
      ImmutableList.of("ready", "testing", "liveStarting", "live");

  private final YouTube youTube;
  private final MightyProperties properties;
  private final Clock clock;

  private List<LiveBroadcast> activeBroadcasts = new ArrayList<>();
  private DateTime lastActiveBroadcastsRefresh = new DateTime(0);

  YouTubeHelper(MightyProperties properties, YouTube youTube, Clock clock) {
    this.youTube = youTube;
    this.properties = properties;
    this.clock = clock;
  }

  /**
   * Returns all current active broadcasts on the channel, filtering out persistent broadcasts if
   * the "{@code ignorePersistentBroadcasts}" module property is {@code true}. An active broadcast
   * is in one of the {@link #BROADCAST_ACTIVE_LIFE_CYCLES} states, which means this method also
   * returns broadcasts that are in the testing or starting life cycle. Returned broadcast are
   * requested using the {@code "snippet,status"} parts.
   * <p>
   * The helper caches the list for {@link #ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS} seconds, and
   * will hold off on refreshing the data after this time is expired until the method is called
   * again.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return a list of active broadcasts, empty if there is none
   * @throws IOException if an error occurred while contacting YouTube
   */
  public List<LiveBroadcast> getActiveBroadcasts() throws IOException {
    if (clock.millis()
        - lastActiveBroadcastsRefresh.getValue() > ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS) {
      logger.debug("Refreshing active broadcasts (ignoring persistent: {})",
          shouldIgnorePersistentBroadcasts());
      YouTube.LiveBroadcasts.List activeRequest = youTube.liveBroadcasts().list("snippet,status");
      activeRequest.setBroadcastStatus("active");
      if (shouldIgnorePersistentBroadcasts()) {
        activeRequest.setBroadcastType("event");
      } else {
        activeRequest.setBroadcastType("all");
      }
      LiveBroadcastListResponse activeResponse = activeRequest.execute();

      activeBroadcasts = getActiveBroadcasts(activeResponse.getItems());
      lastActiveBroadcastsRefresh = new DateTime(clock.millis());
      logger.info("Found {} active broadcast(s)", activeBroadcasts.size());
    }
    return activeBroadcasts;
  }

  /**
   * Returns the most recent live broadcast. A live broadcast is a broadcast that is currently
   * being streamed too. This method does not return broadcasts that are in the ready, testing or
   * starting life cycle.
   * <p>
   * This method uses the same cache described in {@link #getActiveBroadcasts()}.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return the most recent live broadcast
   * @throws IOException if an error occurred while contacting YouTube
   */
  public Optional<LiveBroadcast> getMostRecentLiveBroadcast() throws IOException {
    LiveBroadcast broadcast = null;
    for (LiveBroadcast activeBroadcast : getActiveBroadcasts()) {
      if (!activeBroadcast.getStatus().getLifeCycleStatus().equals("live")) {
        continue;
      }
      if (broadcast == null || broadcast.getSnippet().getActualStartTime()
          .getValue() < activeBroadcast.getSnippet().getActualStartTime().getValue()) {
        broadcast = activeBroadcast;
      }
    }
    return Optional.fromNullable(broadcast);
  }

  /**
   * Returns all live chat ids for currently active broadcasts. An active broadcast is in one of the
   * {@link #BROADCAST_ACTIVE_LIFE_CYCLES} states, which means this method also returns broadcasts
   * that are in the testing or starting life cycle.
   * <p>
   * This method uses the same cache described in {@link #getActiveBroadcasts()}.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return a list of active live chat ids, or an empty list if no active broadcast is detected
   * @throws IOException if an error occurred while contacting YouTube
   */
  public List<String> getActiveLiveChatIds() throws IOException {
    List<String> activeLiveChatIds = new ArrayList<>();
    for (LiveBroadcast liveBroadcast : getActiveBroadcasts()) {
      activeLiveChatIds.add(liveBroadcast.getSnippet().getLiveChatId());
    }
    return activeLiveChatIds;
  }

  /**
   * Returns the live chat id of the most recent live broadcast. A live broadcast is a broadcast
   * that is currently being streamed too. This method does not return broadcasts that are in the
   * ready, testing or starting life cycle.
   * <p>
   * This method uses the same cache described in {@link #getActiveBroadcasts()}.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return the most recent live broadcast live chat id, or an absent optional if no live broadcast
   *         is detected
   * @throws IOException if an error occurred while contacting YouTube
   */
  public Optional<String> getMostRecentLiveBroadcastLiveChatId() throws IOException {
    Optional<LiveBroadcast> liveBroadcast = getMostRecentLiveBroadcast();
    if (!liveBroadcast.isPresent()) {
      return Optional.absent();
    }

    return Optional.of(liveBroadcast.get().getSnippet().getLiveChatId());
  }

  private boolean shouldIgnorePersistentBroadcasts() {
    return "true"
        .equalsIgnoreCase(properties.get(MightyProperty.IGNORE_PERSISTENT_BROADCASTS));
  }

  private List<LiveBroadcast> getActiveBroadcasts(List<LiveBroadcast> liveBroadcasts) {
    List<LiveBroadcast> activeBroadcasts = new ArrayList<>();
    for (LiveBroadcast liveBroadcast : liveBroadcasts) {
      if (BROADCAST_ACTIVE_LIFE_CYCLES.contains(liveBroadcast.getStatus().getLifeCycleStatus())) {
        activeBroadcasts.add(liveBroadcast);
      }
    }
    return activeBroadcasts;
  }
}
