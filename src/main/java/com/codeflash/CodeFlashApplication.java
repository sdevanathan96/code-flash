package com.codeflash;

import com.codeflash.config.EnrichmentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.codeflash.config.LeetCodeConfig;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({LeetCodeConfig.class, EnrichmentConfig.class})
@EnableScheduling
public class CodeFlashApplication {

  public static void main(String[] args) {
    SpringApplication.run(CodeFlashApplication.class, args);
  }
}