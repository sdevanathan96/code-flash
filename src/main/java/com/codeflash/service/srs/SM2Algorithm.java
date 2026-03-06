package com.codeflash.service.srs;

import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;

import java.time.LocalDate;

public class SM2Algorithm implements SRSAlgorithm {

  private static final double EASE_AGAIN_DELTA  = -0.20;
  private static final double EASE_HARD_DELTA   = -0.15;
  private static final double EASE_EASY_DELTA   = +0.15;
  private static final double HARD_MULTIPLIER   =  1.20;
  private static final double EASY_BONUS        =  1.30;
  private static final double MIN_EASE          =  1.30;
  private static final double MAX_EASE          =  2.50;
  private static final int    MIN_INTERVAL      =  1;

  @Override
  public SRSState computeNextState(SRSState current, ConfidenceRating rating) {

    double newEase = switch (rating) {
        case AGAIN -> clampEase(current.getEaseFactor() + EASE_AGAIN_DELTA);
        case HARD  -> clampEase(current.getEaseFactor() + EASE_HARD_DELTA);
        case GOOD  -> current.getEaseFactor();
        case EASY  -> clampEase(current.getEaseFactor() + EASE_EASY_DELTA);
    };
    int newInterval = switch (rating) {
        case AGAIN -> MIN_INTERVAL;
        case HARD  -> enforceMin(round(current.getIntervalDays() * HARD_MULTIPLIER));
        case GOOD  -> enforceMin(round(current.getIntervalDays() * newEase));
        case EASY  -> enforceMin(round(current.getIntervalDays() * newEase * EASY_BONUS));
    };
    return current.withEaseFactor(newEase)
        .withIntervalDays(newInterval)
        .withNextDueDate(LocalDate.now().plusDays(newInterval))
        .withTotalSolves(current.getTotalSolves() + 1);
  }
  private double clampEase(double ease) {
    return Math.min(MAX_EASE, Math.max(MIN_EASE, ease));
  }
  private int enforceMin(int interval) {
    return Math.max(MIN_INTERVAL, interval);
  }
  private int round(double value) {
    return (int) Math.round(value);
  }
}