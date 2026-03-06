package com.codeflash.service.srs;

import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;

public interface SRSAlgorithm {
  SRSState computeNextState(SRSState current, ConfidenceRating rating);
}