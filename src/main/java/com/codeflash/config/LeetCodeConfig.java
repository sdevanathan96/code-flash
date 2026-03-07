package com.codeflash.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "leetcode")
public class LeetCodeConfig {

  private String sessionCookie;
  private String csrfToken;
  private String baseUrl = "https://leetcode.com/graphql";
  private int timeoutSeconds = 10;
}