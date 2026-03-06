package com.codeflash.service;

import com.codeflash.domain.SRSState;
import com.codeflash.dto.request.SolveRequest;
import com.codeflash.dto.response.ProblemResponse;
import com.codeflash.dto.response.SolveRecordResponse;
import com.codeflash.repository.SolveRecordRepository;
import com.codeflash.service.srs.SRSService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolveService {

  private final SRSService srsService;
  private final SolveRecordRepository solveRecordRepository;
  private final ProblemMapper mapper;
  private final ProblemService problemService;

  public ProblemResponse recordSolve(Long problemId, SolveRequest request) {
    SRSState updated = srsService.recordSolve(
        problemId, request.confidence(), request.notes());
    return problemService.getProblemById(problemId);
  }

  @Transactional(readOnly = true)
  public List<SolveRecordResponse> getSolveHistory(Long problemId){
    return solveRecordRepository
       .findByProblemIdOrderBySolvedAtDesc(problemId)
       .stream()
       .map(mapper::toSolveRecordResponse)
       .toList();
  }
}
