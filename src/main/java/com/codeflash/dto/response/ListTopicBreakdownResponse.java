package com.codeflash.dto.response;

public record ListTopicBreakdownResponse(
    String tagName,
    int totalProblems,
    int dueProblems,
    double masteryScore,
    double averageConfidence
) {}
