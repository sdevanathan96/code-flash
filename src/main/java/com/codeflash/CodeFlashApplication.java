package com.codeflash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.codeflash.config.LeetCodeConfig;
import org.springframework.context.annotation.DependsOn;

@SpringBootApplication
@EnableConfigurationProperties(LeetCodeConfig.class)
public class CodeFlashApplication {

  public static void main(String[] args) {
    SpringApplication.run(CodeFlashApplication.class, args);
  }
}