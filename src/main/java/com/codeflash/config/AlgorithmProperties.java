package com.codeflash.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "algorithm")
@Getter
@Setter
public class AlgorithmProperties {

  private Sm2 sm2 = new Sm2();
  private Velocity velocity = new Velocity();

  @Getter @Setter
  public static class Sm2 {
    private double minEaseFactor = 1.3;
    private double maxEaseFactor = 2.5;
    private double initialEaseFactor = 2.5;
    private double againEaseDelta = -0.20;
    private double hardEaseDelta = -0.15;
    private double easyEaseDelta = 0.15;
    private double hardIntervalMultiplier = 1.2;
    private double easyIntervalMultiplier = 1.3;
    private int firstSolveInterval = 1;
    private int secondSolveInterval = 2;
  }

  @Getter @Setter
  public static class Velocity {
    private double baselineDailySolves = 1.0;
    private double minVelocityFactor = 1.0;
    private double maxVelocityFactor = 5.0;
    private int windowDays = 3;
  }
}
