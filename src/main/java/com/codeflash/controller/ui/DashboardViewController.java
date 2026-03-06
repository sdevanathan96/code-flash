package com.codeflash.controller.ui;

import com.codeflash.dto.response.*;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.TagRepository;
import com.codeflash.service.*;
import com.codeflash.service.importer.ImporterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


// ═══════════════════════════════════════════════════════════════════════════
// DashboardViewController — serves /
// ═══════════════════════════════════════════════════════════════════════════
@Controller
@RequiredArgsConstructor
class DashboardViewController {

  private final StatsService statsService;

  @GetMapping("/")
  public String dashboard(Model model) {
    DashboardSummaryResponse summary = statsService.getSummary();

    // Bottom 5 patterns by mastery score — these are the weaknesses to show
    List<PatternStatsResponse> weakPatterns = statsService.getPatternStats()
        .stream()
        .filter(p -> p.totalSolves() > 0)  // only show tags you've attempted
        .limit(5)
        .toList();

    model.addAttribute("summary", summary);
    model.addAttribute("weakPatterns", weakPatterns);
    return "dashboard";  // → templates/dashboard.html
  }
}

