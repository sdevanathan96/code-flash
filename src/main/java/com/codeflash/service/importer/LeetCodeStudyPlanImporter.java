package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.client.LeetCodeUrlParser;
import com.codeflash.domain.ListSource;
import com.codeflash.dto.RawProblemData;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LeetCodeStudyPlanImporter extends ProblemImporter {

  private final LeetCodeGraphQLClient client;
  private final String listUrl;
  private final String planSlug;
  private final String listName;

  public LeetCodeStudyPlanImporter(
      ProblemRepository problemRepository,
      TagRepository tagRepository,
      ProblemListRepository problemListRepository,
      ProblemPersistenceHelper persistenceHelper,
      LeetCodeGraphQLClient client,
      LeetCodeUrlParser urlParser,
      String listUrl) {
    super(problemRepository, tagRepository, problemListRepository, persistenceHelper);
    this.client = client;
    this.listUrl = listUrl;
    this.planSlug = urlParser.extractStudyPlanSlug(listUrl)
        .orElseThrow(() -> new IllegalArgumentException(
            "Not a valid LeetCode study plan URL: " + listUrl));
    List<RawProblemData> problems = client.fetchStudyPlanProblems(planSlug);
    this.listName = Arrays.stream(planSlug.split("-"))
        .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
        .collect(Collectors.joining(" "));
  }
  @Override
  protected List<RawProblemData> fetchRawData() {
    List<RawProblemData> problems = client.fetchStudyPlanProblems(planSlug);
    if (problems.isEmpty()) {
      throw new RuntimeException(
          "No problems returned for study plan: " + planSlug
              + ". Check session cookie.");
    }
    return problems;
  }
  @Override
  protected ProblemListEntity resolveList() {

    return problemListRepository.findByName(listName)
        .orElseGet(() -> problemListRepository.save(
            ProblemListEntity.builder()
                .name(listName)
                .source(ListSource.LEETCODE_STUDY_PLAN)
                .sourceUrl(listUrl)
                .build()
        ));
  }

  @Override
  protected String getListName() { return listName; }

  @Override
  protected ListSource getListSource() { return ListSource.LEETCODE_STUDY_PLAN; }

}

