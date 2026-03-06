
// ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
// ┃  FILE 6a: service/importer/LeetCodeListImporter.java                    ┃
// ┃  Purpose: Imports problems from a user's custom LeetCode list URL       ┃
// ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
//
// ═══════════════════════════════════════════════════════════════════════════
//  HOW THIS DIFFERS FROM Grind150Importer
// ═══════════════════════════════════════════════════════════════════════════
//
// Grind150Importer: stateless — no per-request data, same every run.
//                   Can be a Spring singleton (@Component + ApplicationRunner).
//
// LeetCodeListImporter: stateful per request — the listUrl changes every
//                       time a user imports a different list.
//                       Created fresh each time by ImporterFactory.
//                       NOT a @Component — ImporterFactory uses new.
//
// This is why the ImporterFactory exists. Grind150Importer is a singleton
// that ImporterFactory just returns. LeetCodeListImporter is a prototype
// (new instance per import) that ImporterFactory constructs.
//
// ═══════════════════════════════════════════════════════════════════════════
//  STORING favoriteIdHash ON THE LIST ENTITY
// ═══════════════════════════════════════════════════════════════════════════
//
// When creating the ProblemListEntity for a custom list, we store the
// extracted hash so the "Sync" button can re-fetch without re-pasting:
//
//   ProblemListEntity.builder()
//       .name(listName)
//       .source(ListSource.LEETCODE_CUSTOM)
//       .favoriteIdHash(favoriteIdHash)  ← store for future sync
//       .sourceUrl(listUrl)              ← original pasted URL
//       .build()
//
// ProblemImporter.resolveList() only creates the entity with name + source.
// LeetCodeListImporter overrides resolveList() to include the extra fields.
// This is one of the few cases where overriding a base class helper is right.
//
// ── THINK ABOUT ───────────────────────────────────────────────────────────
// Q: Should fetchRawData() fail fast or return empty on API error?
// A: Fail fast — throw RuntimeException. The base class importProblems()
//    catches it per-problem, but a total fetch failure means rawList is
//    empty or never returned. There's no point continuing an import with
//    zero raw data. The error propagates to run() or the controller which
//    returns a clear error response to the user.
// ──────────────────────────────────────────────────────────────────────────

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

