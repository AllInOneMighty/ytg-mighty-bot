package com.youtube.gaming.mightybot.modules.trivia;

/** Type of trivia question. */
public enum TriviaQuestionType {
  /** A question that can only be answered by "{@code true}" or "{@code false}". */
  BOOLEAN,
  /** A question that shows multiple answers that the players need to choose from. */
  MULTIPLE_CHOICES,
  /** A question that shows no possible answers; players are expected to find it. */
  GUESS;
}
