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


@Controller
@RequiredArgsConstructor
class ProblemsViewController {

  private final ProblemService problemService;
  private final StatsService statsService;
  private final ProblemListRepository problemListRepository;
  private final TagRepository tagRepository;

  @GetMapping("/problems")
  public String problems(
      @RequestParam(required = false) String list,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) String difficulty,
      @RequestParam(required = false, defaultValue = "false") boolean dueOnly,
      Model model) {

    model.addAttribute("lists", problemListRepository.findAll().stream()
        .map(l -> new ListResponse(l.getId(), l.getName(),
            l.getSource().name(), 0))
        .toList());

    model.addAttribute("tags", tagRepository.findAll().stream()
        .map(t -> new TagResponse(t.getId(), t.getName(), 0))
        .sorted(java.util.Comparator.comparing(TagResponse::name))
        .toList());

    model.addAttribute("selectedList", list);
    model.addAttribute("selectedTag", tag);
    model.addAttribute("selectedDiff", difficulty);
    model.addAttribute("dueOnly", dueOnly);

    if (list != null && !list.isBlank()) {
      problemListRepository.findByName(list).ifPresent(l -> {
        model.addAttribute("topicBreakdown",
            statsService.getPatternStats().stream()
                .filter(p -> p.totalProblems() > 0)
                .map(p -> new ListTopicBreakdownResponse(
                    p.tagName(), p.totalProblems(), 0,
                    p.masteryScore(), p.averageConfidence()))
                .toList());
      });
    }

    var diffEnum = difficulty != null && !difficulty.isBlank()
        ? com.codeflash.domain.Difficulty.valueOf(difficulty) : null;

    List<ProblemResponse> problems = dueOnly
        ? problemService.getDueProblems(
        Optional.ofNullable(list), Optional.ofNullable(tag))
        : problemService.getAllProblems(
            Optional.ofNullable(tag),
            Optional.ofNullable(list),
            Optional.ofNullable(diffEnum));

    model.addAttribute("problems", problems);
    return "problems";
  }

  @GetMapping("/problems/{id}")
  public String problemDetail(@PathVariable Long id, Model model) {
    model.addAttribute("problem", problemService.getProblemById(id));
    return "problem-detail";
  }
}
