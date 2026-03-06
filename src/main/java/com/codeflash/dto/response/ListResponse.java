package com.codeflash.dto.response;

public record ListResponse(
    Long id,
    String name,
    String source,
    int problemCount
) {}
