package com.codeflash.domain;

import java.util.Objects;

public enum Difficulty {
  EASY, MEDIUM, HARD;

  public static Difficulty fromApi(String apiValue){
    if (Objects.isNull(apiValue) || apiValue.isBlank()) {
      throw new IllegalArgumentException("Invalid API value: " + apiValue);
    }
    apiValue = apiValue.trim().toUpperCase();
    return Difficulty.valueOf(apiValue.trim().toUpperCase());
  }
}
