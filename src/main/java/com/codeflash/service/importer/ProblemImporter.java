package com.codeflash.service.importer;

import com.codeflash.domain.ListSource;
import com.codeflash.dto.ImportResult;
import com.codeflash.dto.RawProblemData;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.entity.ProblemListEntity;
import com.codeflash.entity.TagEntity;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class ProblemImporter {

  protected final ProblemRepository problemRepository;
  protected final TagRepository tagRepository;
  protected final ProblemListRepository problemListRepository;

  protected final ProblemPersistenceHelper persistenceHelper;


  public final ImportResult importProblems(){
    List<RawProblemData> rawList = fetchRawData();
    log.info("Fetched {} raw problems for list '{}'", rawList.size(), getListName());
    ProblemListEntity list = resolveList();
    int imported = 0, skipped = 0, failed = 0;
    List<String> failedSlugs = new ArrayList<>();
    for (RawProblemData raw : rawList) {
        try {
            if (problemRepository.existsBySlug(raw.slug())) {
              persistenceHelper.linkToListIfAbsent(raw.slug(), list);
              skipped++;
            } else {
                persistenceHelper.saveOneProblem(raw, list);
                imported++;
            }
        } catch (Exception e) {
            log.error("Failed to import slug '{}': {}", raw.slug(), e.getMessage());
            failedSlugs.add(raw.slug());
            failed++;
        }
    }
    return ImportResult.builder()
        .imported(imported)
        .skipped(skipped)
        .failed(failed)
        .failedSlugs(failedSlugs)
        .listName(getListName())
        .build();
  }

  protected abstract List<RawProblemData> fetchRawData();

  protected abstract String getListName();

  protected abstract ListSource getListSource();

  private Set<TagEntity> resolveTags(List<String> tagNames){
  return tagNames.stream()
      .map(name -> tagRepository.findByNameIgnoreCase(name)
         .orElseGet(() -> tagRepository.save(
             TagEntity.builder().name(name).build()
         )))
     .collect(Collectors.toSet());
  }
  protected ProblemListEntity resolveList(){
    return problemListRepository.findByName(getListName())
       .orElseGet(() -> problemListRepository.save(
           ProblemListEntity.builder()
               .name(getListName())
               .source(getListSource())
               .build()
       ));
  }
}