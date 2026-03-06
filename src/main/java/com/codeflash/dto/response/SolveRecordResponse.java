package com.codeflash.dto.response;

import com.codeflash.domain.ConfidenceRating;
import java.time.LocalDateTime;

public record SolveRecordResponse(
    Long id,
    Long problemId,
    ConfidenceRating confidence,
    String notes,
    LocalDateTime solvedAt
) {}
