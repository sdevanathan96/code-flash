package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.client.LeetCodeUrlParser;
import com.codeflash.domain.ListSource;
import com.codeflash.dto.RawProblemData;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ManualImporter extends ProblemImporter {

  private static final String MANUAL_LIST_NAME = "Manual Imports";

  private final LeetCodeGraphQLClient client;
  private final String slug;

  public ManualImporter(
   ProblemRepository problemRepository,
   TagRepository tagRepository,
   ProblemListRepository problemListRepository,
   ProblemPersistenceHelper persistenceHelper,
   LeetCodeGraphQLClient client,
   LeetCodeUrlParser urlParser,
   String input){
    super(problemRepository, tagRepository, problemListRepository, persistenceHelper);
    this.client = client;
    this.slug = urlParser.extractProblemSlug(input)
       .orElseThrow(() -> new IllegalArgumentException(
           "Not a valid problem slug or URL: " + input));
  }

  @Override
  protected List<RawProblemData> fetchRawData() {
    return client
        .fetchProblemMetadata(slug)
        .map(raw -> List.of(raw))
        .orElseThrow(() -> new RuntimeException("Problem not found on LeetCode: " + slug));
  }

  @Override
  protected String getListName() { return MANUAL_LIST_NAME; }

  @Override
  protected ListSource getListSource() { return ListSource.LEETCODE_CUSTOM; }
}