package com.codeflash.service.srs;

import com.codeflash.config.AlgorithmProperties;
import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;
import com.codeflash.repository.SolveRecordRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class VelocityAdjustedAlgorithm implements SRSAlgorithm {

  private final SM2Algorithm sm2Algorithm;
  private final SolveRecordRepository solveRecordRepository;
  private final AlgorithmProperties algorithmProperties;

  @Override
  public SRSState computeNextState(SRSState current, ConfidenceRating rating) {
    var sm2Cfg = algorithmProperties.getSm2();

    if (current.getTotalSolves() == 0) {
      return sm2Algorithm.computeNextState(current, rating)
          .withIntervalDays(sm2Cfg.getFirstSolveInterval())
          .withNextDueDate(LocalDate.now().plusDays(sm2Cfg.getFirstSolveInterval()));
    }
    if (current.getTotalSolves() == 1) {
      int interval = rating.getScore() >= 2
          ? sm2Cfg.getSecondSolveInterval() : 1;
      return sm2Algorithm.computeNextState(current, rating)
          .withIntervalDays(interval)
          .withNextDueDate(LocalDate.now().plusDays(interval));
    }

    SRSState base = sm2Algorithm.computeNextState(current, rating);
    double factor = computeVelocityFactor();
    int adjusted = Math.max(1,
        (int) Math.round(base.getIntervalDays() / factor));
    return base
        .withIntervalDays(adjusted)
        .withNextDueDate(LocalDate.now().plusDays(adjusted));
  }

  private double computeVelocityFactor() {
    var cfg = algorithmProperties.getVelocity();
    long recentSolves = solveRecordRepository
        .countBySolvedAtAfter(
            LocalDateTime.now().minusDays(cfg.getWindowDays()));
    double dailyAvg = recentSolves / (double) cfg.getWindowDays();
    double raw = dailyAvg / cfg.getBaselineDailySolves();
    return Math.min(cfg.getMaxVelocityFactor(),
        Math.max(cfg.getMinVelocityFactor(), raw));
  }
}