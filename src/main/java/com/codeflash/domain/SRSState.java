package com.codeflash.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDate;

@Value
@Builder
@With
public class SRSState {

  Long id;
  Long problemId;
  int intervalDays;
  double easeFactor;
  LocalDate nextDueDate;
  int totalSolves;

  public static SRSState newFor(Long problemId) {
    return SRSState.builder().id(null).problemId(problemId).intervalDays(1).easeFactor(2.5).nextDueDate(LocalDate.now()).totalSolves(0).build();
  }
  public boolean isDueToday() {
    return !nextDueDate.isAfter(LocalDate.now());
  }
  public boolean isNew(){
    return totalSolves == 0;
  }
  public boolean isMature() {
    return intervalDays > 21;
  }
}