// ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
// ┃  FILE 4: service/ProblemService.java                                    ┃
// ┃  Purpose: Business logic for problem browsing and review queue          ┃
// ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
//
// ═══════════════════════════════════════════════════════════════════════════
//  CONCEPTUAL DEEP DIVE: Weighted random selection
// ═══════════════════════════════════════════════════════════════════════════
//
// getWeightedRandom() surfaces a problem for unstructured practice.
// Pure random (Collections.shuffle + get(0)) treats all problems equally.
// Weighted random biases toward problems that need more attention.
//
// Weight formula per problem:
//
//   weight = (daysSinceLastSolve + 1) * difficultyMultiplier * (1 / avgConfidence)
//
//   daysSinceLastSolve:   problems not seen recently rank higher
//   difficultyMultiplier: HARD=3, MEDIUM=2, EASY=1 (harder problems surface more)
//   1 / avgConfidence:    low confidence (AGAIN/HARD ratings) = higher weight
//                         (avoid division by zero: use max(0.5, avgConfidence))
//
// Implementation — weighted reservoir sampling:
//   1. For each problem, compute its weight
//   2. Generate a random key: Math.pow(random.nextDouble(), 1.0 / weight)
//   3. Select the problem with the highest key
//
// This is O(n) and gives exact weighted probabilities without building
// a cumulative distribution array.
//
// ═══════════════════════════════════════════════════════════════════════════
//  getDueProblems: scoped vs unscoped
// ═══════════════════════════════════════════════════════════════════════════
//
// When listName is present → findDueProblemsInList (scoped to one list)
// When listName is absent  → findDueProblems (all due problems)
//
// The service handles this via Optional<String>:
//
//   public List<ProblemResponse> getDueProblems(Optional<String> listName) {
//       List<ProblemEntity> entities = listName
//           .map(problemRepository::findDueProblemsInList)
//           .orElseGet(problemRepository::findDueProblems);
//       ...
//   }
//
// The controller extracts the listName from the query param and wraps it
// in Optional before passing to the service. The service has no knowledge
// of HTTP — it just receives an Optional<String>.
//
// ── THINK ABOUT ───────────────────────────────────────────────────────────
// Q: getDueProblems loads ProblemEntity but needs SRSStateEntity for the
//    response. How do we avoid N+1 queries?
// A: findDueProblems already JOINs srs_states (it filters by nextDueDate).
//    Add a JOIN FETCH for the SRSState in the same query using @EntityGraph,
//    OR load SRS states for all due problem IDs in one batch query:
//
//      List<Long> ids = entities.stream().map(ProblemEntity::getId).toList();
//      Map<Long, SRSStateEntity> srsMap = srsStateRepository
//          .findByProblemIdIn(ids)            ← one query, all states
//          .stream()
//          .collect(toMap(SRSStateEntity::getProblemId, identity()));
//
//    Then: mapper.toResponse(entity, srsMap.get(entity.getId()))
//    One batch query. No N+1.
// ──────────────────────────────────────────────────────────────────────────

package com.codeflash.service;

import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.exception.ResourceNotFoundException;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.SRSStateRepository;
import com.codeflash.repository.TagRepository;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.domain.Difficulty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

  private final ProblemRepository problemRepository;
  private final SRSStateRepository srsStateRepository;
  private final TagRepository tagRepository;
  private final ProblemListRepository problemListRepository;
  private final ProblemMapper mapper;

  @Transactional(readOnly = true)
  public List<ProblemResponse> getDueProblems(Optional<String> listName) {

    List<ProblemEntity> entities = listName
      .map(problemRepository::findDueProblemsInList)
      .orElseGet(problemRepository::findDueProblems);
    List<Long> ids = entities.stream().map(ProblemEntity::getId).toList();
    Map<Long, SRSStateEntity> srsMap = srsStateRepository
        .findByProblemIdIn(ids).stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, Function.identity()));

    return entities.stream()
        .map(e -> mapper.toResponse(e, srsMap.get(e.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProblemResponse> getAllProblems(
      Optional<String> tag, Optional<String> list,
      Optional<Difficulty> difficulty) {
    List<ProblemEntity> all = problemRepository.findAllWithTagsAndLists();
    return all.stream()
       .filter(e -> tag.map(t -> e.getTags().stream()
               .anyMatch(te -> te.getName().equalsIgnoreCase(t)))
           .orElse(true))
       .filter(e -> list.map(l -> e.getLists().stream()
               .anyMatch(le -> le.getName().equalsIgnoreCase(l)))
           .orElse(true))
       .filter(e -> difficulty.map(d -> e.getDifficulty() == d)
           .orElse(true))
       .map(mapper::toResponse)
       .toList();
  }

  @Transactional(readOnly = true)
  public Optional<ProblemResponse> getWeightedRandom(Optional<String> listName){
    List<ProblemEntity> candidates = listName
        .map(problemRepository::findByListName)
        .orElseGet(() -> problemRepository.findAllWithTagsAndLists());
    if (candidates.isEmpty()) return Optional.empty();
    List<Long> ids = candidates.stream().map(ProblemEntity::getId).toList();
    Map<Long, SRSStateEntity> srsMap = srsStateRepository
        .findByProblemIdIn(ids).stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, Function.identity()));
    Random random = new Random();
    ProblemEntity selected = null;
    double maxKey = -1;

    for (ProblemEntity e : candidates) {
        SRSStateEntity srs = srsMap.get(e.getId());
        double weight = computeWeight(e, srs);
        double key = Math.pow(random.nextDouble(), 1.0 / weight);
        if (key > maxKey) { maxKey = key; selected = e; }
    }
    return Optional.of(mapper.toResponse(selected, srsMap.get(selected.getId())));
  }

  @Transactional(readOnly = true)
  public ProblemResponse getProblemById(Long id){
    ProblemEntity entity = problemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Problem with id: " + id));
    SRSStateEntity srs = srsStateRepository.findByProblemId(id)
        .orElse(null);
    return srs != null ? mapper.toResponse(entity, srs) : mapper.toResponse(entity);
  }

  private double computeWeight(ProblemEntity entity, SRSStateEntity srs) {

    long daysSince =
        srs == null ? 30 : ChronoUnit.DAYS.between(srs.getNextDueDate(), LocalDate.now());
    double difficultyMult =
        switch (entity.getDifficulty()) {
          case HARD -> 3.0;
          case MEDIUM -> 2.0;
          case EASY -> 1.0;
        };
    double avgConfidence = srs == null ? 0.5 : Math.max(0.5, srs.getEaseFactor() / 2.5 * 3.0);
    return (daysSince + 1) * difficultyMult * (1.0 / avgConfidence);
  }

  @Transactional(readOnly = true)
  public List<ProblemResponse> getDueProblems(
      Optional<String> listName, Optional<String> tagName) {

    List<ProblemEntity> entities;

    if (listName.isPresent() && tagName.isPresent()) {
      entities = problemRepository.findDueProblemsInListAndTag(
          listName.get(), tagName.get());
    } else if (listName.isPresent()) {
      entities = problemRepository.findDueProblemsInList(listName.get());
    } else
      entities = tagName.map(s -> problemRepository.findDueProblems().stream()
          .filter(p -> p.getTags().stream()
              .anyMatch(t -> t.getName().equalsIgnoreCase(s)))
          .toList()).orElseGet(problemRepository::findDueProblems);

    List<Long> ids = entities.stream().map(ProblemEntity::getId).toList();
    List<SRSStateEntity> srsStates = srsStateRepository.findByProblemIdIn(ids);
    Map<Long, SRSStateEntity> srsMap = srsStates.stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, e -> e));

    return entities.stream()
        .map(e -> mapper.toResponse(e, srsMap.get(e.getId())))
        .toList();
  }
}