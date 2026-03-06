package com.codeflash.service.srs;

import com.codeflash.domain.ConfidenceRating;
import com.codeflash.domain.SRSState;
import com.codeflash.entity.SRSStateEntity;
import com.codeflash.entity.SolveRecordEntity;
import com.codeflash.repository.SRSStateRepository;
import com.codeflash.repository.SolveRecordRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SRSService {

  private final SRSAlgorithm algorithm;
  private final SRSStateRepository srsStateRepository;
  private final SolveRecordRepository solveRecordRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public SRSState recordSolve(Long problemId, ConfidenceRating rating, String notes) {
    SRSState current = srsStateRepository
        .findByProblemId(problemId)
        .map(this::toDomain)
        .orElse(SRSState.newFor(problemId));
    SRSState updated = algorithm.computeNextState(current, rating);
    srsStateRepository.save(toEntity(updated));
    SolveRecordEntity record = SolveRecordEntity.builder()
        .problemId(problemId)
        .confidence(rating)
        .notes(notes)
        .solvedAt(LocalDateTime.now())
        .build();
    solveRecordRepository.save(record);
    eventPublisher.publishEvent(
        new SolvedEvent(problemId, rating, updated, LocalDateTime.now())
    );
    return updated;
  }
  public void initializeSRSState(Long problemId) {
    if (srsStateRepository.existsByProblemId(problemId)) return;
    SRSState fresh = SRSState.newFor(problemId);
    srsStateRepository.save(toEntity(fresh));
  }
  public long getDueCount(){
    return srsStateRepository.countByNextDueDateLessThanEqual(LocalDate.now());
  }
  private SRSState toDomain(SRSStateEntity entity) {
    return SRSState.builder()
        .id(entity.getId())
        .problemId(entity.getProblemId())
        .intervalDays(entity.getIntervalDays())
        .easeFactor(entity.getEaseFactor())
        .nextDueDate(entity.getNextDueDate())
        .totalSolves(entity.getTotalSolves())
        .build();
  }

  private SRSStateEntity toEntity(SRSState domain) {
    return SRSStateEntity.builder()
        .id(domain.getId())
        .problemId(domain.getProblemId())
        .intervalDays(domain.getIntervalDays())
        .easeFactor(domain.getEaseFactor())
        .nextDueDate(domain.getNextDueDate())
        .totalSolves(domain.getTotalSolves())
        .build();
  }
}