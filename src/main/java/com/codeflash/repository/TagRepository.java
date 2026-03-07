package com.codeflash.repository;

import com.codeflash.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

  Optional<TagEntity> findByNameIgnoreCase(String name);
  List<TagEntity> findAllByNameIn(List<String> names);
  Optional<TagEntity> findByNameIgnoreCaseAndTagType(String name, String tagType);
  List<TagEntity> findAllByTagType(String tagType);
  @Query("SELECT COUNT(p) FROM ProblemEntity p JOIN p.tags t WHERE t.id = :tagId")
  long countProblemsByTagId(@Param("tagId") Long tagId);
}
