package com.codeflash.service.srs;

import com.codeflash.config.AlgorithmProperties;
import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SM2Algorithm implements SRSAlgorithm {

  private final AlgorithmProperties algorithmProperties;

  @Override
  public SRSState computeNextState(SRSState current, ConfidenceRating rating) {
    var cfg = algorithmProperties.getSm2();
    double ease = current.getEaseFactor();
    int interval = current.getIntervalDays();

    ease += switch (rating) {
      case AGAIN -> cfg.getAgainEaseDelta();
      case HARD  -> cfg.getHardEaseDelta();
      case GOOD  -> 0;
      case EASY  -> cfg.getEasyEaseDelta();
    };
    ease = Math.min(cfg.getMaxEaseFactor(),
        Math.max(cfg.getMinEaseFactor(), ease));

    interval = switch (rating) {
      case AGAIN -> 1;
      case HARD  -> (int) Math.round(interval * cfg.getHardIntervalMultiplier());
      case GOOD  -> (int) Math.round(interval * ease);
      case EASY  -> (int) Math.round(interval * ease * cfg.getEasyIntervalMultiplier());
    };
    interval = Math.max(1, interval);

    return SRSState.builder()
        .id(current.getId())
        .problemId(current.getProblemId())
        .totalSolves(current.getTotalSolves())
        .intervalDays(interval)
        .easeFactor(ease)
        .nextDueDate(LocalDate.now().plusDays(interval))
        .build();
  }
}