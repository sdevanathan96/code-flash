package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.client.LeetCodeUrlParser;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImporterFactory {

  private final Grind150Importer grind150Importer;
  private final ProblemRepository problemRepository;
  private final TagRepository tagRepository;
  private final ProblemListRepository problemListRepository;
  private final ProblemPersistenceHelper persistenceHelper;  // ← add
  private final LeetCodeGraphQLClient leetCodeClient;
  private final LeetCodeUrlParser urlParser;

  public ProblemImporter create(ImportSource source, String param){
    return switch (source) {
       case GRIND_150 ->
           grind150Importer;

       case LEETCODE_LIST ->
           new LeetCodeListImporter(
               problemRepository, tagRepository,
               problemListRepository, persistenceHelper,
               leetCodeClient, urlParser, param
           );

       case LEETCODE_STUDY_PLAN ->
           new LeetCodeStudyPlanImporter(
               problemRepository, tagRepository,
               problemListRepository, persistenceHelper,
               leetCodeClient, urlParser, param
           );

       case MANUAL ->
           new ManualImporter(
               problemRepository, tagRepository,
               problemListRepository, persistenceHelper,
               leetCodeClient, urlParser, param
           );
    };
  }
}