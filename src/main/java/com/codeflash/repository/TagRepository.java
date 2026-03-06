package com.codeflash.repository;

import com.codeflash.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

  Optional<TagEntity> findByNameIgnoreCase(String name);
  List<TagEntity> findAllByNameIn(List<String> names);
}
