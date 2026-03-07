package com.codeflash.repository;

import com.codeflash.entity.SolveRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolveRecordRepository extends JpaRepository<SolveRecordEntity, Long> {

  List<SolveRecordEntity> findByProblemIdOrderBySolvedAtDesc(Long problemId);
  long countBySolvedAtAfter(LocalDateTime cutoff);
  List<SolveRecordEntity> findByProblemIdIn(List<Long> problemIds);

  @Query("""
    SELECT s FROM SolveRecordEntity s
    WHERE s.problemId IN :problemIds
    AND s.solvedAt >= :since
    """)
  List<SolveRecordEntity> findRecentByProblemIds(
      @Param("problemIds") List<Long> problemIds,
      @Param("since") LocalDateTime since
  );

  @Query(value = """
    SELECT DATE(solved_at) as solve_date, COUNT(*) as solve_count
    FROM solve_records
    GROUP BY DATE(solved_at)
    ORDER BY DATE(solved_at)
    """, nativeQuery = true)
  List<Object[]> countSolvesByDate();
}