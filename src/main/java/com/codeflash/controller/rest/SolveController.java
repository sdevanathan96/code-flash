package com.codeflash.controller.rest;

import com.codeflash.dto.request.SolveRequest;
import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.dto.response.SolveRecordResponse;
import com.codeflash.service.SolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class SolveController {

  private final SolveService solveService;

  @PostMapping("/{id}/solve")
  public ResponseEntity<ProblemResponse> recordSolve(
      @PathVariable Long id, @Valid @RequestBody SolveRequest request) {
    return ResponseEntity.ok(solveService.recordSolve(id, request));
  }

  @GetMapping("/{id}/history")
  public ResponseEntity<List<SolveRecordResponse>> getSolveHistory(@PathVariable Long id) {
    return ResponseEntity.ok(solveService.getSolveHistory(id));
  }
}

