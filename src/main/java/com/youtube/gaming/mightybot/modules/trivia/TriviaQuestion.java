package com.youtube.gaming.mightybot.modules.trivia;

import java.io.Serializable;

/** A trivia question. */
public interface TriviaQuestion extends Serializable {
  /** The question that will be shown to users. */
  String getQuestion();

  /** Type of question. Each type works differently. */
  TriviaQuestionType getQuestionType();

  /**
   * The answer expected from players. In case of multiple answers, the most expected one should be
   * returned.
   */
  String getAnswer();

  /**
   * Returns whether the given answer is correct for this question. Depending on the question type,
   * multiple answers can be correct.
   */
  boolean isCorrect(String answer);
}
