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
import com.google.common.collect.ImmutableList;

public class YouTubeHelper {
  private static final Logger logger = LoggerFactory.getLogger(YouTubeHelper.class);

  private static final long ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS = 60000;
  private static final long ACTIVE_PERSISTENT_BROADCASTS_REFRESH_CYCLE_MILLIS = 60000;
  private static final List<String> BROADCAST_ACTIVE_LIFE_CYCLES =
      ImmutableList.of("ready", "testing", "liveStarting", "live");

  private final YouTube youTube;
  private final Clock clock;

  private List<LiveBroadcast> activeBroadcasts = new ArrayList<>();
  private DateTime lastActiveBroadcastsRefresh = new DateTime(0);
  private List<LiveBroadcast> activePersistentBroadcasts = new ArrayList<>();
  private DateTime lastActivePersistentBroadcastsRefresh = new DateTime(0);

  YouTubeHelper(YouTube youTube, Clock clock) {
    this.youTube = youTube;
    this.clock = clock;
  }

  /**
   * Returns all current active <i>non-persistent</i> broadcasts (also called scheduled events) on
   * the channel. An active broadcast is in one of the {@link #BROADCAST_ACTIVE_LIFE_CYCLES} states.
   * Returned broadcast are requested using the {@code "snippet,status"} parts.
   * <p>
   * The helper caches the list for {@link #ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS} seconds, and
   * will hold off on refreshing the data after this time is expired until the method is called
   * again.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return a list of active <i>non-persistent</i> broadcasts, empty if there is none
   * @throws IOException if an error occurred while contacting YouTube
   */
  public List<LiveBroadcast> getActiveBroadcasts() throws IOException {
    if (clock.millis()
        - lastActiveBroadcastsRefresh.getValue() > ACTIVE_BROADCASTS_REFRESH_CYCLE_MILLIS) {
      logger.debug("Refreshing active non-persistent broadcasts");
      YouTube.LiveBroadcasts.List activeRequest = youTube.liveBroadcasts().list("snippet,status");
      activeRequest.setBroadcastStatus("active");
      LiveBroadcastListResponse activeResponse = activeRequest.execute();

      activeBroadcasts = getActiveBroadcasts(activeResponse.getItems());
      lastActiveBroadcastsRefresh = new DateTime(clock.millis());
      logger.info("Found {} active non-persistent broadcast(s)", activeBroadcasts.size());
    }
    return activeBroadcasts;
  }

  /**
   * Returns all current active <i>persistent</i> broadcasts on the channel. An active broadcast is
   * in one of the {@link #BROADCAST_ACTIVE_LIFE_CYCLES} states. Returned broadcast are requested
   * using the {@code "snippet,status"} parts.
   * <p>
   * The helper caches the list for {@link #ACTIVE_PERSISTENT_BROADCASTS_REFRESH_CYCLE_MILLIS}
   * seconds, and will hold off on refreshing the data after this time is expired until the method
   * is called again.
   * <p>
   * This method will only work in a module that requested the
   * {@code https://www.googleapis.com/auth/youtube} OAuth scope.
   *
   * @return a list of active <i>persistent</i> broadcasts, empty if there is none
   * @throws IOException if an error occurred while contacting YouTube
   */
  public List<LiveBroadcast> getActivePersistentBroadcasts() throws IOException {
    if (clock.millis() - lastActivePersistentBroadcastsRefresh
        .getValue() > ACTIVE_PERSISTENT_BROADCASTS_REFRESH_CYCLE_MILLIS) {
      logger.debug("Refreshing active persistent broadcasts");
      YouTube.LiveBroadcasts.List persistentRequest =
          youTube.liveBroadcasts().list("snippet,status");
      persistentRequest.setBroadcastType("persistent");
      persistentRequest.setMine(Boolean.TRUE);
      LiveBroadcastListResponse persistentResponse = persistentRequest.execute();

      activePersistentBroadcasts = getActiveBroadcasts(persistentResponse.getItems());
      lastActivePersistentBroadcastsRefresh = new DateTime(clock.millis());
      logger.info("Found {} active persistent broadcast(s)", activePersistentBroadcasts.size());
    }
    return activePersistentBroadcasts;
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
