package com.youtube.gaming.mightybot.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.common.collect.ImmutableSet;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.properties.MightyProperty;

/**
 * Posts a message in chat on all currently active broadcasts on behalf of the user when someone
 * subscribes to the channel. Saves the names of the new subscribers while the bot is running to
 * avoid announcing the same name several times. This list is reset when the bot is stopped.
 */
public class NewSubChatAnnouncer extends Module {
  private static final Logger logger = LoggerFactory.getLogger(NewSubChatAnnouncer.class);
  private static final Random r = new Random();

  private static final String INTERVAL = "interval";
  private static final int MINIMUM_INTERVAL = 5;
  private static final String MESSAGES_PREFIX = "message";

  private Set<String> alreadySubscribedCache = new HashSet<String>();
  private List<String> messages;
  private DateTime lastPublishedAt;

  @Override
  public void checkProperties() throws InvalidConfigurationException {
    getProperties().throwIfNullOrEmpty(INTERVAL, "Interval can't be empty");
    if (getProperties().getInt(INTERVAL) < MINIMUM_INTERVAL) {
      throw new InvalidConfigurationException(getProperties().addPrefix(INTERVAL),
          "Interval can't be less than 5s");
    }

    getProperties().throwIfNoneByPrefix(MESSAGES_PREFIX);
    List<String> messages = getProperties().getByPrefix(MESSAGES_PREFIX);
    for (String message : messages) {
      int firstMatch = message.indexOf("{name}");
      if (firstMatch == -1 || firstMatch != message.lastIndexOf("{name}")) {
        throw new InvalidConfigurationException(String.format(
            "There must be one and only one occurrence of '{name}' in your message: '%s'",
            message));
      }
    }
  }

  @Override
  public void init() {
    messages = getProperties().getByPrefix(MESSAGES_PREFIX);
  }

  @Override
  public long getIntervalSecond() {
    return getProperties().getInt(INTERVAL);
  }

  @Override
  @Nullable
  public Set<String> getRequiredOauthScopes() {
    return ImmutableSet.of("https://www.googleapis.com/auth/youtube");
  }

  @Override
  public void run(MightyContext context) throws Exception {
    List<String> newSubscribers = getNewSubscribers(context);

    if (!newSubscribers.isEmpty()) {
      List<String> activeLiveChatIds = context.youTubeHelper().getActiveLiveChatIds();
      if (activeLiveChatIds.isEmpty()) {
        logger.info("No active chat where to announce new subs");
        return;
      }

      // For each new subscriber, post to all chats
      for (String newSubscriber : newSubscribers) {
        if (!alreadySubscribedCache.contains(newSubscriber)) {
          for (String liveChatId : activeLiveChatIds) {
            postNewSubscriberMessage(liveChatId, newSubscriber, context);
          }
          alreadySubscribedCache.add(newSubscriber);
        }
      }
    }
  }

  private List<String> getNewSubscribers(MightyContext context) throws IOException {
    YouTube.Subscriptions.List request =
        context.youTube().subscriptions().list("snippet,subscriberSnippet");
    request.setKey(getProperties().get(MightyProperty.API_KEY));
    request.setMyRecentSubscribers(true);
    if (lastPublishedAt == null) {
      request.setMaxResults(2L);
    } else {
      request.setMaxResults(20L);
    }

    SubscriptionListResponse response = request.execute();

    List<String> newSubscribers = new ArrayList<>();

    if (lastPublishedAt == null) {
      if (response.getPageInfo().getTotalResults() > 1) {
        lastPublishedAt = response.getItems().get(0).getSnippet().getPublishedAt();
      }
    } else if (response.getItems().size() > 0) {
      ListIterator<Subscription> iterator =
          response.getItems().listIterator(response.getItems().size());
      while (iterator.hasPrevious()) {
        Subscription subscription = iterator.previous();
        if (subscription.getSnippet().getPublishedAt().getValue() > lastPublishedAt.getValue()) {
          newSubscribers.add(subscription.getSubscriberSnippet().getTitle());
          lastPublishedAt = subscription.getSnippet().getPublishedAt();
        }
      }
    }

    return newSubscribers;
  }

  private void postNewSubscriberMessage(String liveChatId, String subscriberName,
      MightyContext context) throws IOException {
    String messageFormat = messages.get(r.nextInt(messages.size()));
    String message = messageFormat.replace("{name}", subscriberName);

    context.youTubeHelper().postTextMessageToLiveChat(liveChatId, message);
  }
}
