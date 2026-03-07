package com.codeflash.dto.response;

import com.codeflash.domain.Difficulty;

import java.time.LocalDate;
import java.util.Set;

public record ProblemResponse(
   Long id,
   String slug,
   String title,
   Difficulty difficulty,
   String url,
   Set<String> tags,
   Set<String> companyTags,
   Set<String> lists,

   int intervalDays,
   double easeFactor,
   LocalDate nextDueDate,
   int totalSolves,
   boolean isDueToday,
   String dueLabel
) {}



