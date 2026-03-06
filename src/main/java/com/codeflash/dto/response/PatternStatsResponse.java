package com.codeflash.dto.response;

public record PatternStatsResponse(
    String tagName,
    int totalProblems,
    int solvedProblems,
    int totalSolves,
    double averageConfidence,
    double masteryScore,
    double retentionRate,
    double recentTrend,
    String trendDirection
) {}