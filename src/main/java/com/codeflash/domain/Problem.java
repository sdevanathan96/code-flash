package com.codeflash.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Set;

@Value
@Builder
@With
public class Problem {

  Long id;
  String slug;
  String title;
  String url;
  Difficulty difficulty;
  Set<String> tags;
  Set<String> lists;
  LocalDateTime createdAt;

  public boolean isEasy()   { return difficulty == Difficulty.EASY; }
  public boolean isMedium() { return difficulty == Difficulty.MEDIUM; }
  public boolean isHard()   { return difficulty == Difficulty.HARD; }

  public boolean hasTag(String tag) {
    return tags != null && tags.stream()
        .anyMatch(t -> t.equalsIgnoreCase(tag));
  }

  public boolean belongsToList(String listName) {
    return lists != null && lists.stream()
        .anyMatch(l -> l.equalsIgnoreCase(listName));
  }

  public String derivedUrl() {
    if (url != null && !url.isBlank()) return url;
    return "https://leetcode.com/problems/" + slug + "/";
  }
}