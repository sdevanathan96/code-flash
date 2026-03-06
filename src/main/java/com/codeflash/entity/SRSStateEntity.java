package com.codeflash.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "srs_states")
public class SRSStateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "problem_id", nullable = false, unique = true)
  private Long problemId;

  @Column(name = "interval_days", nullable = false)
  private int intervalDays;

  @Column(name = "ease_factor", nullable = false)
  private double easeFactor;

  @Column(name = "next_due_date", nullable = false)
  private LocalDate nextDueDate;

  @Column(name = "total_solves", nullable = false)
  private int totalSolves;
}