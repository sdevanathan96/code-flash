package com.codeflash.controller.rest;

import com.codeflash.dto.response.DashboardSummaryResponse;
import com.codeflash.dto.response.PatternStatsResponse;
import com.codeflash.repository.SolveRecordRepository;
import com.codeflash.service.StatsService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

  private final StatsService statsService;
  private final SolveRecordRepository solveRecordRepository;

  @GetMapping("/summary")
  public ResponseEntity<DashboardSummaryResponse> getSummary() {
    return ResponseEntity.ok(statsService.getSummary());
  }

  @GetMapping("/patterns")
  public ResponseEntity<List<PatternStatsResponse>> getPatternStats() {
    return ResponseEntity.ok(statsService.getPatternStats());
  }

  @GetMapping("/heatmap")
  public ResponseEntity<List<Map<String, Object>>> getHeatmap() {
    List<Object[]> rows = solveRecordRepository.countSolvesByDate();
    List<Map<String, Object>> result = rows.stream()
        .map(r -> Map.of("date", r[0].toString(), "count", r[1]))
        .toList();
    return ResponseEntity.ok(result);
  }
}