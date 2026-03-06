package com.codeflash.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ImportResult(int imported, int skipped, int failed, List<String> failedSlugs, String listName) {

  public ImportResult {
    failedSlugs = failedSlugs != null ? failedSlugs : List.of();
  }

  public boolean hasFailures() {
     return failed > 0;
  }

  public int total() {
     return imported + skipped + failed;
  }
}