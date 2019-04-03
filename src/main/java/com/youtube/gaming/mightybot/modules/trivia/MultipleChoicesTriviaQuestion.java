package com.youtube.gaming.mightybot.modules.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/** A trivia question that shows multiple answers that the players can choose from. */
public class MultipleChoicesTriviaQuestion implements TriviaQuestion {
  private static final long serialVersionUID = -809137867080301493L;

  private String question;
  private List<String> allAnswers;
  private Integer correctAnswerIndex;
  private Integer correctAnswerIndexForHumans;

  /** Creates a new multiple choices trivia question.
   *
   * @param question the question to show to players
   * @param correctAnswer the correct answer
   * @param incorrectAnswers a list of incorrect answers
   */
  public MultipleChoicesTriviaQuestion(String question, String correctAnswer,
      List<String> incorrectAnswers) {
    allAnswers = new ArrayList<String>(1 + incorrectAnswers.size());
    allAnswers.add(correctAnswer);
    allAnswers.addAll(incorrectAnswers);
    Collections.shuffle(allAnswers);

    correctAnswerIndex = new Integer(allAnswers.indexOf(correctAnswer));
    correctAnswerIndexForHumans = correctAnswerIndex + 1;

    this.question = buildQuestion(question, allAnswers);
  }

  private String buildQuestion(String question, List<String> allAnswers) {
    StringBuilder sb = new StringBuilder(question);
    ListIterator<String> answerIterator = allAnswers.listIterator();
    while (answerIterator.hasNext()) {
      sb.append(String.format(" %d) %s", answerIterator.nextIndex() + 1, answerIterator.next()));
    }
    return sb.toString();
  }

  @Override
  public String getQuestion() {
    return question;
  }

  @Override
  public TriviaQuestionType getQuestionType() {
    return TriviaQuestionType.MULTIPLE_CHOICES;
  }

  @Override
  public String getAnswer() {
    return String.format("%d) %s", correctAnswerIndexForHumans, allAnswers.get(correctAnswerIndex));
  }

  @Override
  public boolean isCorrect(String answer) {
    return correctAnswerIndexForHumans.toString().equals(answer);
  }
}
