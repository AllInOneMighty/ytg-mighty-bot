package com.youtube.gaming.mightybot.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.modules.trivia.TriviaQuestion;
import com.youtube.gaming.mightybot.util.DynamicPath;
import com.youtube.gaming.mightybot.util.ObjectStreamUtils;

/**
 * Allows users in chat to play a trivia question game.
 * <p>
 * <b>Work still in progress.</b>
 */
public class Trivia extends Module {
  private static final Logger logger = LoggerFactory.getLogger(Trivia.class);

  private static final String DB_FILE_NAME = "trivia.db";

  private Path dbPath;
  private List<TriviaQuestion> triviaQuestions;

  private boolean isActive;
  private int currentQuestionIndex;

  @Override
  public void checkProperties() throws InvalidConfigurationException {
    dbPath = DynamicPath.locate(DB_FILE_NAME);
    if (!dbPath.toFile().exists()) {
      throw new RuntimeException("Can't find Trivia DB file: " + dbPath.toAbsolutePath());
    }
    if (!dbPath.toFile().canRead()) {
      throw new RuntimeException("Can't read Trivia DB file: " + dbPath.toAbsolutePath());
    }
  }

  @Override
  public void init() {
    isActive = false;
    currentQuestionIndex = -1;
    triviaQuestions = new ArrayList<TriviaQuestion>();

    triviaQuestions = readDatabase(dbPath);

    logger.info("Read {} questions.", triviaQuestions.size());
  }

  @Override
  @Nullable
  public Set<String> getRequiredOauthScopes() {
    return ImmutableSet.of("https://www.googleapis.com/auth/youtube");
  }

  @Override
  public long getIntervalSecond() {
    return 2;
  }

  @Override
  public void run(MightyContext context) throws Exception {
    if (!isActive) {
      currentQuestionIndex = new Random().nextInt(triviaQuestions.size());
      logger.info(triviaQuestions.get(currentQuestionIndex).getQuestion());
      logger.info(triviaQuestions.get(currentQuestionIndex).getAnswer());
      isActive = true;
    }
  }

  public List<TriviaQuestion> readDatabase(Path dbPath) {
    logger.info("Reading Trivia database from: {}", dbPath.toAbsolutePath());
    return ObjectStreamUtils.readObjectStreamFromFile(dbPath);
  }

//  public void postQuestion() {
//    LiveChatMessage content = new LiveChatMessage()
//        .setSnippet(new LiveChatMessageSnippet()
//            .setLiveChatId(liveChatId)
//            .setType("textMessageEvent")
//            .setTextMessageDetails(new LiveChatTextMessageDetails()
//                .setMessageText(message.replace("{name}", subscriberName))));
//
//    YouTube.LiveChatMessages.Insert request =
//        context.youTube().liveChatMessages().insert("snippet", content);
//
//    request.execute();
//  }

}
