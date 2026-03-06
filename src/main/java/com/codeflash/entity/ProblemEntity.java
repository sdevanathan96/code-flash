package com.codeflash.entity;

import com.codeflash.domain.Difficulty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "problems")
public class ProblemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "difficulty")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private Difficulty difficulty;

  @Column(length = 500)
  private String url;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
     name = "problem_tags",
     joinColumns = @JoinColumn(name = "problem_id"),
     inverseJoinColumns = @JoinColumn(name = "tag_id")
  )
  @Builder.Default
  private Set<TagEntity> tags = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
     name = "problem_list_items",
     joinColumns = @JoinColumn(name = "problem_id"),
     inverseJoinColumns = @JoinColumn(name = "problem_list_id")
  )
  @Builder.Default
  private Set<ProblemListEntity> lists = new HashSet<>();

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
     createdAt = LocalDateTime.now();
  }
}