package com.codeflash.controller.ui;

import com.codeflash.dto.response.*;
import com.codeflash.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
class ReviewViewController {

  private final ProblemService problemService;
  private final SolveService solveService;

  @GetMapping("/review")
  public String review(
      @RequestParam(required = false) String list,
      @RequestParam(required = false) String tag,
      Model model) {

    List<ProblemResponse> due = problemService.getDueProblems(
        Optional.ofNullable(list), Optional.ofNullable(tag));

    if (due.isEmpty()) {
      model.addAttribute("problem", null);
      return "review";
    }

    ProblemResponse current = due.get(0);
    List<ProblemResponse> upNext = due.size() > 1
        ? due.subList(1, Math.min(6, due.size())) : List.of();

    var history = solveService.getSolveHistory(current.id());

    String nextUrl = "/review" +
        (list != null ? "?list=" + list : "") +
        (tag != null ? (list != null ? "&" : "?") + "tag=" + tag : "");

    int remaining = due.size();
    model.addAttribute("problem", current);
    model.addAttribute("history", history);
    model.addAttribute("upNext", upNext);
    model.addAttribute("remaining", remaining);
    model.addAttribute("nextUrl", nextUrl);
    model.addAttribute("progress",
        remaining == 0 ? 100 : Math.max(5, 100 - remaining * 2));

    return "review";
  }
}

