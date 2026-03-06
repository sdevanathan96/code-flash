package com.codeflash.repository;

import com.codeflash.domain.Difficulty;
import com.codeflash.entity.ProblemEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

  Optional<ProblemEntity> findBySlug(String slug);
  boolean existsBySlug(String slug);
  List<ProblemEntity> findByDifficulty(Difficulty difficulty);

  @Query("SELECT p FROM ProblemEntity p")
  @EntityGraph(attributePaths = {"tags", "lists"})
  List<ProblemEntity> findAllWithTagsAndLists();

  @Query("""
     SELECT DISTINCT p FROM ProblemEntity p
     JOIN p.tags t
     WHERE t.name = :tag
     """)
  List<ProblemEntity> findByTagName(@Param("tag") String tag);

  @Query("SELECT DISTINCT p FROM ProblemEntity p JOIN p.lists l WHERE l.name = :listName")
  List<ProblemEntity> findByListName(@Param("listName") String listName);

  @Query("""
     SELECT p FROM ProblemEntity p
     JOIN SRSStateEntity s ON s.problemId = p.id
     WHERE s.nextDueDate <= CURRENT_DATE
     ORDER BY s.nextDueDate ASC
     """)
  List<ProblemEntity> findDueProblems();

  @Query("""
     SELECT DISTINCT p FROM ProblemEntity p
     JOIN SRSStateEntity s ON s.problemId = p.id
     JOIN p.lists l
     WHERE s.nextDueDate <= CURRENT_DATE
     AND   l.name = :listName
     ORDER BY s.nextDueDate ASC
     """)
  List<ProblemEntity> findDueProblemsInList(@Param("listName") String listName);

  @Query("""
    SELECT DISTINCT p FROM ProblemEntity p
    JOIN SRSStateEntity s ON s.problemId = p.id
    JOIN p.lists l
    JOIN p.tags t
    WHERE s.nextDueDate <= CURRENT_DATE
    AND l.name = :listName
    AND t.name = :tagName
    ORDER BY s.nextDueDate ASC
    """)
  List<ProblemEntity> findDueProblemsInListAndTag(
      @Param("listName") String listName,
      @Param("tagName") String tagName
  );

  @Query("""
    SELECT DISTINCT p FROM ProblemEntity p
    JOIN p.lists l
    JOIN p.tags t
    WHERE l.name = :listName
    AND t.name = :tagName
    """)
  List<ProblemEntity> findByListNameAndTagName(
      @Param("listName") String listName,
      @Param("tagName") String tagName
  );
}