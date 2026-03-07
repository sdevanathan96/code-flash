package com.codeflash.service;

import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.dto.response.SolveRecordResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.exception.ResourceNotFoundException;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.SRSStateRepository;
import com.codeflash.repository.SolveRecordRepository;
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
  private final SolveRecordRepository solveRecordRepository;

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
        .sorted(Comparator.comparing(e ->
            Optional.ofNullable(srsMap.get(e.getId()))
                .map(SRSStateEntity::getNextDueDate)
                .orElse(LocalDate.now())))
        .map(e -> mapper.toResponse(e, srsMap.get(e.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProblemResponse> getAllProblems(
      Optional<String> tag, Optional<String> list,
      Optional<Difficulty> difficulty) {
    List<ProblemEntity> all = problemRepository.findAllWithTagsAndLists();
    log.info("Total problems loaded: {}", all.size());

    List<ProblemEntity> filtered = all.stream()
        .filter(e -> tag.map(t -> e.getTags().stream()
                .anyMatch(te -> te.getName().equalsIgnoreCase(t)))
            .orElse(true))
        .filter(e -> list.map(l -> e.getLists().stream()
                .anyMatch(le -> le.getName().equalsIgnoreCase(l)))
            .orElse(true))
        .filter(e -> difficulty.map(d -> e.getDifficulty() == d)
            .orElse(true))
        .toList();

    List<Long> ids = filtered.stream().map(ProblemEntity::getId).toList();
    Map<Long, SRSStateEntity> srsMap = srsStateRepository
        .findByProblemIdIn(ids).stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, Function.identity()));

    return filtered.stream()
        .map(e -> mapper.toResponse(e, srsMap.get(e.getId())))
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
    return mapper.toResponse(entity, srs);
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

  @Transactional(readOnly = true)
  public List<SolveRecordResponse> getSolveHistory(Long problemId) {
    return solveRecordRepository.findByProblemIdOrderBySolvedAtDesc(problemId)
        .stream()
        .map(s -> new SolveRecordResponse(
            s.getId(), s.getProblemId(), s.getConfidence(), s.getNotes(), s.getSolvedAt() ))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProblemResponse> getSequentialQueue(
      Optional<String> listName, Optional<String> tagName) {

    List<ProblemEntity> all = problemRepository.findAllWithTagsAndLists();

    Map<Long, Integer> positionMap = listName
        .map(l -> {
          List<Long> ordered = problemRepository.findOrderedProblemIdsByListName(l);
          Map<Long, Integer> pos = new HashMap<>();
          for (int i = 0; i < ordered.size(); i++) pos.put(ordered.get(i), i);
          return pos;
        })
        .orElse(Map.of());

    List<ProblemEntity> filtered = all.stream()
        .filter(e -> tagName.map(t -> e.getTags().stream()
                .anyMatch(te -> te.getName().equalsIgnoreCase(t)))
            .orElse(true))
        .filter(e -> listName.map(l -> e.getLists().stream()
                .anyMatch(le -> le.getName().equalsIgnoreCase(l)))
            .orElse(true))
        .sorted(positionMap.isEmpty()
            ? Comparator.comparing(ProblemEntity::getId)
            : Comparator.comparing(e -> positionMap.getOrDefault(e.getId(), Integer.MAX_VALUE)))
        .toList();

    List<Long> ids = filtered.stream().map(ProblemEntity::getId).toList();
    Map<Long, SRSStateEntity> srsMap = srsStateRepository
        .findByProblemIdIn(ids).stream()
        .collect(Collectors.toMap(SRSStateEntity::getProblemId, Function.identity()));

    return filtered.stream()
        .map(e -> mapper.toResponse(e, srsMap.get(e.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProblemResponse> getMixedQueue(Optional<String> list, Optional<String> tag) {
    List<ProblemResponse> sequential = getSequentialQueue(list, tag);

    List<ProblemResponse> duePool = new ArrayList<>(getDueProblems(
        list, tag)
        .stream()
        .filter(p -> p.totalSolves() > 0)
        .toList());

    if (duePool.isEmpty()) return sequential;

    Collections.shuffle(duePool);
    Iterator<ProblemResponse> dueIter = duePool.iterator();

    List<ProblemResponse> mixed = new ArrayList<>();
    Random random = new Random();

    for (ProblemResponse p : sequential) {
      mixed.add(p);
      if (dueIter.hasNext() && random.nextDouble() < 0.25) {
        mixed.add(dueIter.next());
      }
    }

    dueIter.forEachRemaining(mixed::add);

    return mixed;
  }
}