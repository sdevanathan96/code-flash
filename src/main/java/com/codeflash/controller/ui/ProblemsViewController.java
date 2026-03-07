package com.codeflash.controller.ui;

import com.codeflash.domain.Difficulty;
import com.codeflash.dto.response.*;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.TagRepository;
import com.codeflash.service.*;
import com.codeflash.service.importer.ImporterFactory;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String difficulty,
      @RequestParam(required = false, defaultValue = "false") boolean dueOnly,
      Model model) {
    model.addAttribute("selectedCompany", company);
    model.addAttribute("lists", problemListRepository.findAll().stream()
        .map(l -> new ListResponse(
            l.getId(), l.getName(), l.getSource().name(),
            (int) problemListRepository.countProblemsByListId(l.getId())))
        .toList());

    model.addAttribute("tags", tagRepository.findAllByTagType("TOPIC").stream()
        .map(t -> new TagResponse(
            t.getId(), t.getName(),
            (int) tagRepository.countProblemsByTagId(t.getId())))
        .sorted(Comparator.comparing(TagResponse::name))
        .toList());

    model.addAttribute("selectedList", list);
    model.addAttribute("selectedTag", tag);
    model.addAttribute("selectedDiff", difficulty);
    model.addAttribute("dueOnly", dueOnly);

    var diffEnum = difficulty != null && !difficulty.isBlank()
        ? Difficulty.valueOf(difficulty) : null;

    List<ProblemResponse> problems = dueOnly
        ? problemService.getDueProblems(
        Optional.ofNullable(list).filter(s -> !s.isBlank()))
        : problemService.getAllProblems(
            Optional.ofNullable(tag).filter(s -> !s.isBlank()),
            Optional.ofNullable(list).filter(s -> !s.isBlank()),
            Optional.ofNullable(diffEnum));
    if (company != null && !company.isBlank()) {
      problems = problems.stream()
          .filter(p -> p.companyTags().stream()
              .anyMatch(c -> c.equalsIgnoreCase(company)))
          .toList();
    }

    model.addAttribute("problems", problems);

    if (list != null && !list.isBlank()) {
      var today = LocalDate.now();
      var breakdown = problems.stream()
          .flatMap(p -> p.tags().stream().map(t -> Map.entry(t, p)))
          .collect(Collectors.groupingBy(Map.Entry::getKey,
              Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
          .entrySet().stream()
          .map(e -> {
            var tagProblems = e.getValue();
            long due = tagProblems.stream()
                .filter(ProblemResponse::isDueToday)
                .count();
            double mastery = tagProblems.stream()
                .mapToDouble(p -> p.totalSolves() > 0 ? 1.0 : 0.0)
                .average().orElse(0.0);
            return new ListTopicBreakdownResponse(
                e.getKey(),
                tagProblems.size(),
                (int) due,
                mastery,
                0.0);
          })
          .filter(t -> t.totalProblems() > 0)
          .sorted(Comparator.comparingInt(ListTopicBreakdownResponse::totalProblems).reversed())
          .toList();

      model.addAttribute("topicBreakdown", breakdown);
    }
    return "problems";
  }


  @GetMapping("/problems/random")
  public String randomProblem(
      @RequestParam(required = false) String list,
      RedirectAttributes attrs) {

    return problemService
        .getWeightedRandom(Optional.ofNullable(list))
        .map(p -> "redirect:/problems/" + p.id())
        .orElse("redirect:/problems");
  }

  @GetMapping("/problems/{id}")
  public String problemDetail(@PathVariable Long id, Model model) {
    model.addAttribute("problem", problemService.getProblemById(id));
    model.addAttribute("history", problemService.getSolveHistory(id));
    return "problem-detail";
  }
}
