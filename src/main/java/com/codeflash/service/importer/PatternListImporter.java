package com.codeflash.service.importer;

import com.codeflash.domain.ListSource;
import com.codeflash.dto.ImportResult;
import com.codeflash.dto.RawProblemData;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(3)
public class PatternListImporter extends ProblemImporter implements ApplicationRunner {

  private final ObjectMapper objectMapper;

  @Value("classpath:seed/patterns.json")
  private Resource patternResource;

  public PatternListImporter(
      ProblemRepository problemRepository,
      TagRepository tagRepository,
      ProblemListRepository problemListRepository,
      ProblemPersistenceHelper persistenceHelper,
      ObjectMapper objectMapper) {
    super(problemRepository, tagRepository, problemListRepository, persistenceHelper);
    this.objectMapper = objectMapper;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      if (problemListRepository.findByName("Pattern Problems").isPresent()) {
        log.info("Pattern list already seeded. Skipping.");
        return;
      }
      ImportResult result = importProblems();
      log.info("Pattern seed complete: {} imported, {} skipped, {} failed",
          result.imported(), result.skipped(), result.failed());
    } catch (Exception e) {
      log.error("Pattern seed failed", e);
    }
  }

  @Override
  protected List<RawProblemData> fetchRawData() {
    try (InputStream is = patternResource.getInputStream()) {
      return objectMapper.readValue(is, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to read patterns.json", e);
    }
  }

  @Override
  protected String getListName() { return "Pattern Problems"; }

  @Override
  protected ListSource getListSource() { return ListSource.BUNDLED; }
}
