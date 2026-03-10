package com.codeflash.controller.ui;

import com.codeflash.domain.ReviewMode;
import com.codeflash.dto.response.*;
import com.codeflash.entity.AppSettingsEntity;
import com.codeflash.repository.AppSettingsRepository;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.service.*;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
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
  private final ProblemListRepository problemListRepository;
  private final AppSettingsRepository appSettingsRepository;

  @GetMapping("/review")
  public String review(
      @RequestParam(required = false) String list,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) ReviewMode mode,
      @RequestParam(required = false, defaultValue = "0") int index,
      HttpSession session,
      Model model) {
    if (mode == null) {
      model.addAttribute("selectedList", list);
      model.addAttribute("selectedTag", tag);
      model.addAttribute("mode", null);
      model.addAttribute("lists", problemListRepository.findAll().stream()
          .map(l -> new ListResponse(l.getId(), l.getName(), l.getSource().name(), 0))
          .toList());
      return "review";
    }
    model.addAttribute("selectedList", list);
    model.addAttribute("selectedTag", tag);
    model.addAttribute("mode", mode);
    String sessionKey = "queue:" + mode + ":" +
        Objects.toString(list, "") + ":" + Objects.toString(tag, "");
    String queueKey = "review_position:" + mode + ":" + Objects.toString(list, "");

    List<ProblemResponse> queue = index == 0 ? null :
        (List<ProblemResponse>) session.getAttribute(sessionKey);

    if (queue == null) {
      queue = switch (mode) {
        case DUE_ONLY   -> problemService.getDueProblems(
            Optional.ofNullable(list), Optional.ofNullable(tag));
        case SEQUENTIAL -> problemService.getSequentialQueue(
            Optional.ofNullable(list), Optional.ofNullable(tag));
        case MIXED      -> problemService.getMixedQueue(
            Optional.ofNullable(list), Optional.ofNullable(tag));
      };
      session.setAttribute(sessionKey, queue);
      index = appSettingsRepository.findById(queueKey)
          .map(s -> Integer.parseInt(s.getValue()))
          .orElse(0);
    }

    if (queue.isEmpty() || index >= queue.size()) {
      session.removeAttribute(sessionKey);
      appSettingsRepository.deleteById(queueKey);
      model.addAttribute("problem", null);
      return "review";
    }

    ProblemResponse current = queue.get(index);
    List<ProblemResponse> upNext = queue.subList(
        Math.min(index + 1, queue.size()),
        Math.min(index + 6, queue.size()));

    int remaining = queue.size() - index;
    int total = queue.size();
    int done = index;

    String nextUrl = "/review?mode=" + mode +
        (list != null ? "&list=" + list : "") +
        (tag  != null ? "&tag="  + tag  : "") +
        "&index=" + (index + 1);

    model.addAttribute("problem",   current);
    model.addAttribute("history",   solveService.getSolveHistory(current.id()));
    model.addAttribute("upNext",    upNext);
    model.addAttribute("remaining", remaining);
    model.addAttribute("nextUrl",   nextUrl);
    model.addAttribute("mode",      mode);
    model.addAttribute("progress",  total == 0 ? 100 : (int)((done * 100.0) / total));
    appSettingsRepository.save(new AppSettingsEntity(queueKey, String.valueOf(index)));
    return "review";
  }
}

