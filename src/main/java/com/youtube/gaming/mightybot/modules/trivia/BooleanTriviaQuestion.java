package com.youtube.gaming.mightybot.modules.trivia;

/** A trivia question to which only "{@code true}" or "{@code false}" can be answered. */
public class BooleanTriviaQuestion implements TriviaQuestion {
  private String question;
  private Boolean answer;

  /** Creates a new boolean trivia question.
   *
   * @param question the question to show to players
   * @param answer the correct answer to the question
   */
  public BooleanTriviaQuestion(String question, boolean answer) {
    this.question = question;
    this.answer = new Boolean(answer);
  }

  @Override
  public String getQuestion() {
    return question;
  }

  @Override
  public TriviaQuestionType getQuestionType() {
    return TriviaQuestionType.BOOLEAN;
  }

  @Override
  public String getAnswer() {
    return answer.toString();
  }

  @Override
  public boolean isCorrect(String answer) {
    return answer.equalsIgnoreCase(answer);
  }
}
