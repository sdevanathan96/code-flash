// ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
// ┃  FILE 1a: entity/TagEntity.java                                         ┃
// ┃  Purpose: JPA entity for the tags table                                 ┃
// ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
//
// ═══════════════════════════════════════════════════════════════════════════
//  CONCEPTUAL DEEP DIVE: Why TagEntity is so simple
// ═══════════════════════════════════════════════════════════════════════════
//
// TagEntity is intentionally minimal — just an id and a name. It exists
// purely to normalize tag storage. Without it, tags would be stored as
// a VARCHAR array in the problems table, making queries like
// "give me all problems tagged Dynamic Programming" painful.
//
// With a proper join table (problem_tags), that query is a clean JOIN.
//
// Note there is NO back-reference to problems here:
//
//   // We do NOT have this:
//   @ManyToMany(mappedBy = "tags")
//   private Set<ProblemEntity> problems;
//
// We never navigate Tag → Problems in this app. We always go the other
// way: Problem → Tags. Adding the back-reference would just be dead code
// that Hibernate has to manage for no benefit.
// Bidirectional mappings add complexity — only add them when you need
// navigation in both directions.
//
// ── THINK ABOUT ───────────────────────────────────────────────────────────
// Q: Should TagEntity have equals/hashCode based on id or name?
// A: Name. Two TagEntity objects with the same name are the same tag
//    regardless of their DB id. This matters when Hibernate compares
//    entities in a Set<TagEntity> — using id-based equality would cause
//    duplicates before the first persist (when id is still null).
//    Lombok @EqualsAndHashCode(of = "name") on @Data achieves this,
//    but since we use @Data which bases it on all fields by default,
//    we override with a specific field. See implementation note below.
// ──────────────────────────────────────────────────────────────────────────

package com.codeflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "name")
@ToString(of = {"id", "name"})
@Entity
@Table(name = "tags")
public class TagEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;
}