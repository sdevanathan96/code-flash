package com.codeflash.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
    String error,
    String message,
    LocalDateTime timestamp
) {}
