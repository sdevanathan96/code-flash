
package com.codeflash.controller.rest;

import com.codeflash.dto.response.ListResponse;
import com.codeflash.dto.response.ListTopicBreakdownResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.entity.TagEntity;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.SRSStateRepository;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListController {

  private final ProblemListRepository problemListRepository;
  private final ProblemRepository problemRepository;
  private final SRSStateRepository srsStateRepository;

  @GetMapping
  public ResponseEntity<List<ListResponse>> getLists() {
    List<ListResponse> lists = problemListRepository.findAll().stream()
        .map(l -> new ListResponse(
            l.getId(), l.getName(), l.getSource().name(),
            problemRepository.findByListName(l.getName()).size()
        ))
        .toList();
    return ResponseEntity.ok(lists);
  }

  @GetMapping("/{listId}/topics")
  public ResponseEntity<List<ListTopicBreakdownResponse>> getTopics(
      @PathVariable Long listId) {

    var list = problemListRepository.findById(listId)
        .orElseThrow(() -> new java.util.NoSuchElementException("List not found: " + listId));

    List<ProblemEntity> problems = problemRepository.findByListName(list.getName());

    List<String> tagNames = problems.stream()
        .flatMap(p -> p.getTags().stream().map(TagEntity::getName))
        .distinct().toList();

    List<Long> problemIds = problems.stream().map(ProblemEntity::getId).toList();
    Map<Long, SRSStateEntity> srsMap = srsStateRepository
        .findByProblemIdIn(problemIds).stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, e -> e));

    LocalDate today = LocalDate.now();

    List<ListTopicBreakdownResponse> breakdown = tagNames.stream()
        .map(tag -> {
          List<ProblemEntity> tagProblems = problems.stream()
              .filter(p -> p.getTags().stream()
                  .anyMatch(t -> t.getName().equals(tag)))
              .toList();

          int total = tagProblems.size();

          int due = (int) tagProblems.stream()
              .filter(p -> {
                SRSStateEntity srs = srsMap.get(p.getId());
                return srs != null && !srs.getNextDueDate().isAfter(today);
              })
              .count();

          long solved = tagProblems.stream()
              .filter(p -> {
                SRSStateEntity srs = srsMap.get(p.getId());
                return srs != null && srs.getTotalSolves() > 0;
              }).count();

          double solvedRatio = total == 0 ? 0 : (double) solved / total;

          double avgConf = tagProblems.stream()
              .map(p -> srsMap.get(p.getId()))
              .filter(s -> s != null && s.getTotalSolves() > 0)
              .mapToDouble(s -> s.getEaseFactor() / 2.5 * 3.0)
              .average().orElse(0);

          double mastery = (solvedRatio * 0.4) + (avgConf / 3.0 * 0.6);

          return new ListTopicBreakdownResponse(tag, total, due, mastery, avgConf);
        })
        .sorted(Comparator.comparingDouble(ListTopicBreakdownResponse::masteryScore))
        .toList();

    return ResponseEntity.ok(breakdown);
  }
}
