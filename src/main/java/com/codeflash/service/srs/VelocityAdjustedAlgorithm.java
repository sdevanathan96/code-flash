package com.codeflash.service.srs;

import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;
import com.codeflash.repository.SolveRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class VelocityAdjustedAlgorithm implements SRSAlgorithm {

  private static final double BASELINE_DAILY_SOLVES = 3.0;
  private static final double MIN_VELOCITY_FACTOR   = 0.5;
  private static final double MAX_VELOCITY_FACTOR   = 3.0;
  private static final int    VELOCITY_WINDOW_DAYS  = 7;

  private final SM2Algorithm sm2Algorithm;
  private final SolveRecordRepository solveRecordRepository;

  @Override
  public SRSState computeNextState(SRSState current, ConfidenceRating rating) {
    SRSState base = sm2Algorithm.computeNextState(current, rating);
    double factor = computeVelocityFactor();
    int adjustedInterval = Math.max(1,
        (int) Math.round(base.getIntervalDays() / factor));
    LocalDate adjustedDueDate = LocalDate.now().plusDays(adjustedInterval);
    return base
        .withIntervalDays(adjustedInterval)
        .withNextDueDate(adjustedDueDate);
  }

  private double computeVelocityFactor() {
    long recentSolves = solveRecordRepository
        .countBySolvedAtAfter(
            LocalDateTime.now().minusDays(VELOCITY_WINDOW_DAYS)
        );
    double recentDailyAvg = recentSolves / (double) VELOCITY_WINDOW_DAYS;
    double rawFactor = recentDailyAvg / BASELINE_DAILY_SOLVES;
    return Math.min(MAX_VELOCITY_FACTOR, Math.max(MIN_VELOCITY_FACTOR, rawFactor));
  }
}