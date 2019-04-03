package com.youtube.gaming.mightybot.modules;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
import com.youtube.gaming.mightybot.modules.trivia.BooleanTriviaQuestion;
import com.youtube.gaming.mightybot.modules.trivia.GuessTriviaQuestion;
import com.youtube.gaming.mightybot.modules.trivia.MultipleChoicesTriviaQuestion;
import com.youtube.gaming.mightybot.modules.trivia.TriviaQuestion;
import com.youtube.gaming.mightybot.modules.trivia.TriviaQuestionType;
import com.youtube.gaming.mightybot.util.DynamicPath;

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

    try (InputStream input = new FileInputStream(dbPath.toAbsolutePath().toFile());
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
      readDatabase(reader);
    } catch (IOException e) {
      logger.error("Could not Trivia DB", e);
    }

    logger.info("Read {} questions Trivia DB from: {}", triviaQuestions.size(),
        dbPath.toAbsolutePath());
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

  public void readDatabase(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }

      if (line.equalsIgnoreCase(TriviaQuestionType.BOOLEAN.name())) {
        String question = assertStringNonEmpty(reader.readLine(), "boolean question is missing");
        Boolean answer = assertStringBoolean(reader.readLine());
        triviaQuestions.add(new BooleanTriviaQuestion(question, answer));
      } else if (line.equalsIgnoreCase(TriviaQuestionType.MULTIPLE_CHOICES.name())) {
        String question =
            assertStringNonEmpty(reader.readLine(), "multiple choices question is missing");
        String correctAnswer =
            assertStringNonEmpty(reader.readLine(),
                "multiple choices question correct answer is missing");
        List<String> incorrectAnswers = new ArrayList<String>();
        // At least one incorrect answer must be provided
        incorrectAnswers.add(assertStringNonEmpty(reader.readLine(),
            "multiple choices question has no incorrect answer provided"));
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty()) {
            break;
          }
          incorrectAnswers.add(line);
        }
        triviaQuestions
            .add(new MultipleChoicesTriviaQuestion(question, correctAnswer, incorrectAnswers));
      } else if (line.equalsIgnoreCase(TriviaQuestionType.GUESS.name())) {
        String question = assertStringNonEmpty(reader.readLine(), "guess question is missing");
        List<String> correctAnswers = new ArrayList<String>();
        // At least one correct answer must be provided
        correctAnswers.add(assertStringNonEmpty(reader.readLine(),
            "guess question has no correct answer provided"));
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty()) {
            break;
          }
          correctAnswers.add(line);
        }
        triviaQuestions.add(new GuessTriviaQuestion(question, correctAnswers));
      }
    }

    Collections.shuffle(triviaQuestions);
  }

  public String assertStringNonEmpty(String str, String errorMsg) {
    if (str == null || str.isEmpty()) {
      throw new RuntimeException("Expected string, but got null or empty: " + errorMsg);
    }
    return str;
  }

  public Boolean assertStringBoolean(String str) {
    assertStringNonEmpty(str, "'true' or 'false' expected");
    if (!"true".equalsIgnoreCase(str) && !"false".equalsIgnoreCase(str)) {
      throw new RuntimeException("Expected 'true' or 'false', but found: " + str);
    }
    return Boolean.valueOf(str);
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
