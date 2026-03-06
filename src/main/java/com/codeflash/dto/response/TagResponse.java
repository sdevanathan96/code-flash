package com.codeflash.dto.response;

public record TagResponse(
    Long id,
    String name,
    int problemCount
) {}
