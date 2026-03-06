package com.codeflash.controller.ui;

import com.codeflash.dto.response.*;
import com.codeflash.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
class StatsViewController {

  private final StatsService statsService;

  @GetMapping("/stats")
  public String stats(Model model) {
    model.addAttribute("summary", statsService.getSummary());
    model.addAttribute("patterns", statsService.getPatternStats());
    return "stats";
  }
}

