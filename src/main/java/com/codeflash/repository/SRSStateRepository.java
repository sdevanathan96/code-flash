package com.codeflash.repository;

import com.codeflash.entity.SRSStateEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SRSStateRepository extends JpaRepository<SRSStateEntity, Long> {

  Optional<SRSStateEntity> findByProblemId(Long problemId);
  List<SRSStateEntity> findByProblemIdIn(List<Long> problemIds);
  boolean existsByProblemId(Long problemId);
  long countByNextDueDateLessThanEqual(LocalDate date);
}