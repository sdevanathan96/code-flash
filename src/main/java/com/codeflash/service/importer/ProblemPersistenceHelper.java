package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.config.EnrichmentConfig;
import com.codeflash.domain.Difficulty;
import com.codeflash.dto.RawProblemData;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.entity.TagEntity;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import com.codeflash.service.srs.SRSService;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemPersistenceHelper {

  private final ProblemRepository problemRepository;
  private final TagRepository tagRepository;
  private final SRSService srsService;
  private final EntityManager entityManager;
  private final EnrichmentConfig enrichmentConfig;

  @Transactional
  public void saveOneProblem(RawProblemData raw, ProblemListEntity list) {
    Set<TagEntity> topicTags = resolveTags(raw.tags());
    Set<TagEntity> companyTags = resolveTags(raw.companyTags());
    topicTags.addAll(companyTags);  // merge into one set

    ProblemEntity entity = ProblemEntity.builder()
        .slug(raw.slug())
        .title(raw.title())
        .difficulty(Difficulty.fromApi(raw.difficulty()))
        .url("https://leetcode.com/problems/" + raw.slug() + "/")
        .tags(topicTags)
        .lists(Set.of(list))
        .build();
    ProblemEntity saved = problemRepository.save(entity);
    srsService.initializeSRSState(saved.getId());
  }

  private Set<TagEntity> resolveTags(List<String> tagNames) {
    return tagNames.stream()
        .map(name -> tagRepository.findByNameIgnoreCase(name)
            .orElseGet(() -> tagRepository.save(
                TagEntity.builder().name(name).build()
            )))
        .collect(Collectors.toSet());
  }
  @Transactional
  public void linkToListIfAbsent(String slug, ProblemListEntity list) {
    ProblemEntity entity = problemRepository.findBySlug(slug).orElseThrow();
    if (!entity.getLists().contains(list)) {
      entity.getLists().add(list);
      problemRepository.save(entity);
    }
  }

  @Transactional
  public void enrichProblemFromApi(String slug, LeetCodeGraphQLClient client) {
    ProblemEntity entity = problemRepository.findBySlug(slug).orElse(null);
    if (entity == null) return;

    client.fetchProblemMetadata(slug).ifPresent(raw -> {
      Set<TagEntity> existingTags = new HashSet<>(entity.getTags());
      Set<TagEntity> topicTags = resolveTags(raw.tags());
      Set<TagEntity> companyTags = resolveTags(raw.companyTags());
      existingTags.addAll(topicTags);
      existingTags.addAll(companyTags);
      entity.setTags(existingTags);
      entity.setDifficulty(Difficulty.fromApi(raw.difficulty()));
      problemRepository.save(entity);
    });
  }

  @Transactional
  public void enrichAllProblems(LeetCodeGraphQLClient client) {
    int batchSize = enrichmentConfig.getBatchSize();

    Map<String, TagEntity> tagCache = tagRepository.findAll().stream()
        .collect(Collectors.toMap(
            t -> t.getName().toLowerCase(),
            t -> t,
            (a, b) -> a
        ));

    int page = 0;
    int totalEnriched = 0;

    while (true) {
      List<ProblemEntity> batch = problemRepository
          .findAll(PageRequest.of(page, batchSize))
          .getContent();

      if (batch.isEmpty()) break;

      for (ProblemEntity p : batch) {
        try {
          client.fetchProblemMetadata(p.getSlug()).ifPresent(raw -> {
            Set<TagEntity> merged = new HashSet<>(p.getTags());
            merged.addAll(resolveTagsFromCache(raw.tags(), tagCache));
            merged.addAll(resolveTagsFromCache(raw.companyTags(), tagCache));
            p.setTags(merged);
            p.setDifficulty(Difficulty.fromApi(raw.difficulty()));
          });
          totalEnriched++;
          Thread.sleep(200);
        } catch (Exception e) {
          log.warn("Failed to enrich {}: {}", p.getSlug(), e.getMessage());
        }
      }

      entityManager.flush();
      entityManager.clear();
      log.info("Enriched {} problems so far...", totalEnriched);
      page++;
    }

    log.info("Enrichment complete: {} total", totalEnriched);
  }

  private Set<TagEntity> resolveTagsFromCache(
      List<String> tagNames, Map<String, TagEntity> tagCache) {

    Set<TagEntity> result = new HashSet<>();
    for (String name : tagNames) {
      String key = name.toLowerCase();
      TagEntity tag = tagCache.computeIfAbsent(key, k -> {
        TagEntity newTag = tagRepository.saveAndFlush(
            TagEntity.builder().name(name).build()
        );
        return newTag;
      });
      result.add(tag);
    }
    return result;
  }
}
