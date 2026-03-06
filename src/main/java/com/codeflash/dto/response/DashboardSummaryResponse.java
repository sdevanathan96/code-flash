package com.codeflash.dto.response;

public record DashboardSummaryResponse(
    long dueToday,
    int currentStreak,
    int totalSolves,
    int totalProblems
) {}
