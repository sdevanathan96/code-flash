package com.codeflash.service;

import com.codeflash.domain.Problem;
import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.dto.response.SolveRecordResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.entity.SolveRecordEntity;
import com.codeflash.entity.TagEntity;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    LocalDate due = (srs != null && srs.getNextDueDate() != null)
        ? srs.getNextDueDate()
        : null;

    return new ProblemResponse(
        entity.getId(), entity.getSlug(), entity.getTitle(),
        entity.getDifficulty(),
        entity.getUrl() != null ? entity.getUrl()
            : "https://leetcode.com/problems/" + entity.getSlug() + "/",
        entity.getTags().stream()
            .filter(t -> "TOPIC".equals(t.getTagType()))
            .map(TagEntity::getName)
            .collect(Collectors.toSet()),
        entity.getTags().stream()
            .filter(t -> "COMPANY".equals(t.getTagType()))
            .map(TagEntity::getName)
            .collect(Collectors.toSet()),
        entity.getLists().stream()
            .map(ProblemListEntity::getName)
            .collect(Collectors.toSet()),
        srs != null ? srs.getIntervalDays() : 0,
        srs != null ? srs.getEaseFactor() : 2.5,
        due,
        srs != null ? srs.getTotalSolves() : 0,
        due != null && !due.isAfter(LocalDate.now()),
        deriveDueLabel(due)
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

  private String deriveDueLabel(LocalDate due) {
    if (due == null) return "Not started";
    LocalDate today = LocalDate.now();
    if (due.isBefore(today)) return "Overdue";
    if (due.isEqual(today)) return "Today";
    if (due.isEqual(today.plusDays(1))) return "Tomorrow";
    return "In " + ChronoUnit.DAYS.between(today, due) + " days";
  }
}