package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.client.LeetCodeUrlParser;
import com.codeflash.client.dto.FavoriteListInfo;
import com.codeflash.domain.ListSource;
import com.codeflash.dto.RawProblemData;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LeetCodeListImporter extends ProblemImporter {

  private final LeetCodeGraphQLClient client;
  private final String listUrl;
  private final String favoriteIdHash;
  private final String listName;

  public LeetCodeListImporter(
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
    this.favoriteIdHash = urlParser.extractListHash(listUrl)
        .orElseThrow(() -> new IllegalArgumentException(
            "Not a valid LeetCode list URL: " + listUrl));
    List<FavoriteListInfo> userLists = client.fetchUserLists();
    this.listName = userLists.stream()
        .filter(l -> l.idHash().equals(this.favoriteIdHash))
        .map(FavoriteListInfo::name)
        .findFirst()
        .orElse("Custom List " + favoriteIdHash);
  }
  @Override
  protected List<RawProblemData> fetchRawData() {

    List<RawProblemData> problems = client.fetchListProblems(favoriteIdHash);
    if (problems.isEmpty()) {
      throw new RuntimeException(
          "No problems returned for list hash: "
              + favoriteIdHash
              + ". Check session cookie or list visibility.");
    }
    return problems;
  }
  @Override
  protected ProblemListEntity resolveList() {

    return problemListRepository
        .findByFavoriteIdHash(favoriteIdHash)
        .orElseGet(
            () ->
                problemListRepository.save(
                    ProblemListEntity.builder()
                        .name(listName)
                        .source(ListSource.LEETCODE_CUSTOM)
                        .favoriteIdHash(favoriteIdHash)
                        .sourceUrl(listUrl)
                        .build()));
  }

  @Override
  protected String getListName() { return listName; }

  @Override
  protected ListSource getListSource() { return ListSource.LEETCODE_CUSTOM; }
}

