package com.codeflash.config;

import com.codeflash.repository.SolveRecordRepository;
import com.codeflash.service.srs.SM2Algorithm;
import com.codeflash.service.srs.SRSAlgorithm;
import com.codeflash.service.srs.VelocityAdjustedAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AlgorithmConfig {

  @Value("${algorithm.type:sm2}")
  private String algorithmType;

  private final AlgorithmProperties algorithmProperties;

  @Bean
  public SRSAlgorithm srsAlgorithm(SolveRecordRepository solveRecordRepository) {
    return switch (algorithmType.toLowerCase()) {
      case "sm2"      -> new SM2Algorithm(algorithmProperties);
      case "velocity" -> new VelocityAdjustedAlgorithm(
          new SM2Algorithm(algorithmProperties),
          solveRecordRepository,
          algorithmProperties);
      default -> throw new IllegalStateException(
          "Unknown algorithm type: '" + algorithmType + "'");
    };
  }
}