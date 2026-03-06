package com.codeflash.repository;

import com.codeflash.domain.ListSource;
import com.codeflash.entity.ProblemListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemListRepository extends JpaRepository<ProblemListEntity, Long> {

  Optional<ProblemListEntity> findByName(String name);

  List<ProblemListEntity> findBySource(ListSource source);

  Optional<ProblemListEntity> findByFavoriteIdHash(String hash);
}