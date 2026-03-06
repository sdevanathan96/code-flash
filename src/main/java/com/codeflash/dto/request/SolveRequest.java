package com.codeflash.dto.request;

import com.codeflash.domain.ConfidenceRating;
import jakarta.validation.constraints.NotNull;

public record SolveRequest(
   @NotNull(message = "Confidence rating is required")
   ConfidenceRating confidence,

   String notes
) {}