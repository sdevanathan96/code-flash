package com.codeflash.service.importer;

import com.codeflash.domain.ListSource;
import com.codeflash.dto.ImportResult;
import com.codeflash.dto.RawProblemData;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(2)
public class Grind175Importer extends ProblemImporter implements ApplicationRunner {

  private final ObjectMapper objectMapper;

  @Value("classpath:seed/grind175.json")
  private Resource grindResource;

  private record Grind175Json(List<Grind175Problem> data) {}

  private record Grind175Problem(
      String slug,
      String title,
      String difficulty,
      @JsonProperty("pattern") List<String> tags
  ) {}

  public Grind175Importer(
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
      long count = problemRepository.count();
      if (count > 0) {
        log.info("DB already seeded ({} problems). Skipping Grind 175 import.", count);
        return;
      }
      ImportResult result = importProblems();
      log.info("Grind 175 seed complete: {} imported, {} skipped, {} failed",
          result.imported(), result.skipped(), result.failed());
      if (result.hasFailures()) {
        log.warn("Failed slugs: {}", result.failedSlugs());
      }
    } catch (Exception e) {
      log.error("Grind 175 seed failed — app will start without seed data", e);
    }
  }

  @Override
  protected List<RawProblemData> fetchRawData() {
    try (InputStream is = grindResource.getInputStream()) {
      Grind175Json wrapper = objectMapper.readValue(is, Grind175Json.class);
      return wrapper.data().stream()
          .filter(p -> !p.tags().isEmpty())
          .map(p -> new RawProblemData(
              p.slug(),
              p.title(),
              p.difficulty(),
              p.tags(),
              List.of()
          ))
          .toList();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read classpath resource: grind175.json", e);
    }
  }
  @Override
  protected String getListName() { return "Grind 175"; }

  @Override
  protected ListSource getListSource() { return ListSource.BUNDLED; }
}