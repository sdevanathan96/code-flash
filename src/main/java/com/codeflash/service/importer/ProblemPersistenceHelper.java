package com.codeflash.service.importer;

import com.codeflash.domain.Difficulty;
import com.codeflash.dto.RawProblemData;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.entity.TagEntity;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import com.codeflash.service.srs.SRSService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemPersistenceHelper {

  private final ProblemRepository problemRepository;
  private final TagRepository tagRepository;
  private final SRSService srsService;

  @Transactional
  public void saveOneProblem(RawProblemData raw, ProblemListEntity list) {
    Set<TagEntity> tags = resolveTags(raw.tags());
    ProblemEntity entity = ProblemEntity.builder()
        .slug(raw.slug())
        .title(raw.title())
        .difficulty(Difficulty.fromApi(raw.difficulty()))
        .url("https://leetcode.com/problems/" + raw.slug() + "/")
        .tags(tags)
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
}
