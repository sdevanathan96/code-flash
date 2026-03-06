package com.codeflash.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "enrichment")
public class EnrichmentConfig {
  private boolean enabled = true;
  private int batchSize = 50;
  private int intervalDays = 5;
}
