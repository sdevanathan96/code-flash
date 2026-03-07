package com.codeflash.service;

import com.codeflash.dto.response.DashboardSummaryResponse;
import com.codeflash.dto.response.PatternStatsResponse;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.SolveRecordEntity;
import com.codeflash.entity.TagEntity;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.SolveRecordRepository;
import com.codeflash.repository.SRSStateRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

  private final ProblemRepository problemRepository;
  private final SolveRecordRepository solveRecordRepository;
  private final SRSStateRepository srsStateRepository;

  @Transactional(readOnly = true)
  public DashboardSummaryResponse getSummary() {
    long dueToday = srsStateRepository
        .countByNextDueDateLessThanEqual(LocalDate.now());

    long totalProblems = problemRepository.count();

    List<SolveRecordEntity> allSolves = solveRecordRepository.findAll();
    int totalSolves = allSolves.size();

    int streak = computeStreak(allSolves);

    return new DashboardSummaryResponse(dueToday, streak, totalSolves, (int) totalProblems);
  }

  private int computeStreak(List<SolveRecordEntity> solves) {
    if (solves.isEmpty()) return 0;

    List<LocalDate> solveDates = solves.stream()
        .map(s -> s.getSolvedAt().toLocalDate())
        .distinct()
        .sorted(java.util.Comparator.reverseOrder())
        .toList();

    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    if (!solveDates.get(0).equals(today) && !solveDates.get(0).equals(yesterday)) {
      return 0;
    }

    int streak = 1;
    for (int i = 1; i < solveDates.size(); i++) {
      if (solveDates.get(i).equals(solveDates.get(i - 1).minusDays(1))) {
        streak++;
      } else {
        break;
      }
    }
    return streak;
  }


  @Transactional(readOnly = true)
  public List<PatternStatsResponse> getPatternStats() {
    List<SolveRecordEntity> allSolves = solveRecordRepository.findAll();
    List<ProblemEntity> allProblems = problemRepository.findAllWithTagsAndLists();

    if (allProblems.isEmpty()) return List.of();

    Map<Long, List<String>> problemTags = allProblems.stream()
        .collect(Collectors.toMap(
            ProblemEntity::getId,
            p -> p.getTags().stream().map(t -> t.getName()).toList()
        ));

    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
    List<SolveRecordEntity> recentSolves = allSolves.stream()
        .filter(s -> s.getSolvedAt().isAfter(sevenDaysAgo))
        .toList();

    List<String> allTags = allProblems.stream()
        .flatMap(p -> p.getTags().stream()
            .filter(t -> "TOPIC".equals(t.getTagType()))
            .map(TagEntity::getName))
        .distinct().sorted().toList();

    return allTags.stream().map(tag -> {
          List<Long> tagProblemIds = allProblems.stream()
              .filter(p -> p.getTags().stream().anyMatch(t -> t.getName().equals(tag)))
              .map(ProblemEntity::getId).toList();

          int totalProblems = tagProblemIds.size();

          List<SolveRecordEntity> tagSolves = allSolves.stream()
              .filter(s -> tagProblemIds.contains(s.getProblemId()))
              .toList();

          if (tagSolves.isEmpty()) {
            return new PatternStatsResponse(
                tag, totalProblems, 0, 0, 0, 0, 0, 0, "new");
          }

          int totalSolves = tagSolves.size();
          int solvedProblems = (int) tagSolves.stream()
              .map(SolveRecordEntity::getProblemId).distinct().count();

          double avgConf = tagSolves.stream()
              .mapToInt(s -> s.getConfidence().getScore())
              .average().orElse(0);

          long passingCount = tagSolves.stream()
              .filter(s -> s.getConfidence().isPassingGrade()).count();
          double retentionRate = (double) passingCount / totalSolves;

          double solvedRatio = (double) solvedProblems / totalProblems;
          double masteryScore = (solvedRatio * 0.4) + (avgConf / 3.0 * 0.6);

          List<SolveRecordEntity> recentTagSolves = recentSolves.stream()
              .filter(s -> tagProblemIds.contains(s.getProblemId()))
              .toList();

          double recentAvg = recentTagSolves.isEmpty() ? avgConf :
              recentTagSolves.stream()
                  .mapToInt(s -> s.getConfidence().getScore())
                  .average().orElse(avgConf);

          double trend = recentAvg - avgConf;
          String trendDir = recentTagSolves.isEmpty() ? "new" :
              trend > 0.2 ? "improving" :
                  trend < -0.2 ? "declining" : "stable";

          return new PatternStatsResponse(
              tag, totalProblems, solvedProblems, totalSolves,
              avgConf, masteryScore, retentionRate, trend, trendDir
          );
        })
        .sorted(Comparator.comparingDouble(PatternStatsResponse::masteryScore))
        .toList();
  }
}