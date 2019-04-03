package com.youtube.gaming.mightybot.modules.trivia;

import java.util.List;

import com.google.common.collect.ImmutableList;

/** A trivia question where players have to guess the answer. */
public class GuessTriviaQuestion implements TriviaQuestion {
  private static final long serialVersionUID = -7115857620711982094L;

  private String question;
  private List<String> correctAnswers;

  /**
   * Creates a new "guess" trivia question.
   *
   * @param question the question to show to players
   * @param correctAnswers a list of possible correct answers (can only be one too)
   */
  public GuessTriviaQuestion(String question, List<String> correctAnswers) {
    this.question = question;
    this.correctAnswers = ImmutableList.copyOf(correctAnswers);
  }

  @Override
  public String getQuestion() {
    return question;
  }

  @Override
  public TriviaQuestionType getQuestionType() {
    return TriviaQuestionType.GUESS;
  }

  @Override
  public String getAnswer() {
    return correctAnswers.get(0);
  }

  @Override
  public boolean isCorrect(String answer) {
    for (String correctAnswer : correctAnswers) {
      if (correctAnswer.equalsIgnoreCase(answer)) {
        return true;
      }
    }
    return false;
  }
}
