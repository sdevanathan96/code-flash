package com.codeflash.service;

import com.codeflash.domain.Problem;
import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.dto.response.SolveRecordResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.entity.SolveRecordEntity;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class ProblemMapper {

  public Problem toDomain(ProblemEntity entity) {

    return Problem.builder()
        .id(entity.getId())
        .slug(entity.getSlug())
        .title(entity.getTitle())
        .difficulty(entity.getDifficulty())
        .url(entity.getUrl())
        .tags(entity.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()))
        .lists(entity.getLists().stream().map(l -> l.getName()).collect(Collectors.toSet()))
        .createdAt(entity.getCreatedAt())
        .build();
  }

  public ProblemResponse toResponse(ProblemEntity entity, SRSStateEntity srs) {
    LocalDate due = srs.getNextDueDate();
    return new ProblemResponse(
        entity.getId(), entity.getSlug(), entity.getTitle(),
        entity.getDifficulty(),
        entity.getUrl() != null ? entity.getUrl()
            : "https://leetcode.com/problems/" + entity.getSlug() + "/",
        entity.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()),
        entity.getLists().stream().map(l -> l.getName()).collect(Collectors.toSet()),
        srs.getIntervalDays(), srs.getEaseFactor(), due,
        srs.getTotalSolves(),
        !due.isAfter(LocalDate.now()),
        deriveDueLabel(due)
    );
  }

  public ProblemResponse toResponse(ProblemEntity entity){
    return new ProblemResponse(
       entity.getId(), entity.getSlug(), entity.getTitle(),
       entity.getDifficulty(), entity.getUrl(),
       entity.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()),
       entity.getLists().stream().map(l -> l.getName()).collect(Collectors.toSet()),
       0, 2.5, null, 0, false, "-"
    );
  }

  public SolveRecordResponse toSolveRecordResponse(SolveRecordEntity entity) {
    return new SolveRecordResponse(
        entity.getId(),
        entity.getProblemId(),
        entity.getConfidence(),
        entity.getNotes(),
        entity.getSolvedAt());
  }

  private String deriveDueLabel(LocalDate date) {
    if (date == null) return "—";
    LocalDate today = LocalDate.now();
    if (date.isBefore(today))              return "Overdue";
    if (date.equals(today))                return "Today";
    if (date.equals(today.plusDays(1)))    return "Tomorrow";
    return date.format(DateTimeFormatter.ofPattern("MMM d"));
  }
}