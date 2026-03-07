package com.codeflash.controller.ui;

import com.codeflash.dto.response.*;
import com.codeflash.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequiredArgsConstructor
class DashboardViewController {

  private final StatsService statsService;

  @GetMapping("/")
  public String dashboard(Model model) {
    DashboardSummaryResponse summary = statsService.getSummary();

    List<PatternStatsResponse> weakPatterns = statsService.getPatternStats()
        .stream()
        .filter(p -> p.totalSolves() > 0)
        .limit(5)
        .toList();

    model.addAttribute("summary", summary);
    model.addAttribute("weakPatterns", weakPatterns);
    return "dashboard";
  }
}

