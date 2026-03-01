package com.codeflash.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfidenceRating {

   AGAIN(0, "Again"),
   HARD(1, "Hard"),
   GOOD(2, "Good"),
   EASY(3, "Easy");

  private final int score;
  private final String label;

  public static ConfidenceRating fromScore(int score) {
    ConfidenceRating confidenceRating = null;
    for (ConfidenceRating rating : ConfidenceRating.values()) {
      if (rating.score == score) {
        confidenceRating = rating;
        break;
      }
    }
    if (confidenceRating == null) {
      throw new IllegalArgumentException("Invalid confidence score: " + score + ". Must be 0-3.");
    }
    return confidenceRating;
  }

  public boolean isPassingGrade() {
    return this == GOOD || this == EASY;
  }
}