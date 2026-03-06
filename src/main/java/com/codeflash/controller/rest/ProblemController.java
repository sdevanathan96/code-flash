package com.codeflash.controller.rest;

import com.codeflash.domain.Difficulty;
import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

  private final ProblemService problemService;

  @GetMapping
  public ResponseEntity<List<ProblemResponse>> getAllProblems(
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) String list,
      @RequestParam(required = false) Difficulty difficulty) {
    return ResponseEntity.ok(
        problemService.getAllProblems(
            Optional.ofNullable(tag), Optional.ofNullable(list), Optional.ofNullable(difficulty)));
  }

  @GetMapping("/random")
  public ResponseEntity<ProblemResponse> getWeightedRandom(
      @RequestParam(required = false) String list) {

    return problemService
        .getWeightedRandom(Optional.ofNullable(list))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {

    return ResponseEntity.ok(problemService.getProblemById(id));
  }

  @GetMapping("/due")
  public ResponseEntity<List<ProblemResponse>> getDueProblems(
      @RequestParam(required = false) String list,
      @RequestParam(required = false) String tag) {
    return ResponseEntity.ok(
        problemService.getDueProblems(
            Optional.ofNullable(list),
            Optional.ofNullable(tag)));
  }
}
