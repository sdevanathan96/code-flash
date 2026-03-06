package com.codeflash.service.srs;

import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;

import java.time.LocalDateTime;

  public record SolvedEvent(
     Long problemId,
     ConfidenceRating rating,
     SRSState updatedState,
     LocalDateTime solvedAt
  ) {}