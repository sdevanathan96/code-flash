package com.codeflash.entity;

import com.codeflash.domain.ConfidenceRating;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "solve_records")
public class SolveRecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "problem_id", nullable = false, updatable = false)
  private Long problemId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, columnDefinition = "confidence")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ConfidenceRating confidence;

  @Column(columnDefinition = "TEXT", updatable = false)
  private String notes;

  @Column(name = "solved_at", nullable = false, updatable = false)
  private LocalDateTime solvedAt;

  @PrePersist
  protected void onCreate() {
     if (solvedAt == null) solvedAt = LocalDateTime.now();
  }
}