package com.codeflash.client;

import static com.codeflash.service.importer.ImportSource.LEETCODE_LIST;
import static com.codeflash.service.importer.ImportSource.LEETCODE_STUDY_PLAN;
import static com.codeflash.service.importer.ImportSource.MANUAL;

import com.codeflash.service.importer.ImportSource;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LeetCodeUrlParser {

  private static final Pattern LIST_PATTERN =
     Pattern.compile("leetcode\\.com/list/(?<hash>[a-zA-Z0-9]+)");

  private static final Pattern STUDY_PLAN_PATTERN =
     Pattern.compile("leetcode\\.com/studyplan/(?<slug>[a-zA-Z0-9-]+)");

  private static final Pattern PROBLEM_PATTERN =
     Pattern.compile("leetcode\\.com/problems/(?<slug>[a-zA-Z0-9-]+)");


  public Optional<String> extractListHash(String url) {
    Matcher matcher = LIST_PATTERN.matcher(url);
    return matcher.find() ? Optional.of(matcher.group("hash")) : Optional.empty();
  }

  public Optional<String> extractStudyPlanSlug(String url) {
    Matcher matcher = STUDY_PLAN_PATTERN.matcher(url);
    return matcher.find() ? Optional.of(matcher.group("hash")) : Optional.empty();
  }

  public Optional<String> extractProblemSlug(String url){
    Matcher matcher = PROBLEM_PATTERN.matcher(url);
    return matcher.find() ? Optional.of(matcher.group("hash")) : Optional.empty();
  }

  public Optional<ImportSource> detectUrlType(String url) {
    if (LIST_PATTERN.matcher(url).find())         return Optional.of(LEETCODE_LIST);
    if (STUDY_PLAN_PATTERN.matcher(url).find())   return Optional.of(LEETCODE_STUDY_PLAN);
    if (PROBLEM_PATTERN.matcher(url).find())      return Optional.of(MANUAL);
    if (!url.contains("/"))                       return Optional.of(MANUAL);
    return Optional.empty();
  }
}
