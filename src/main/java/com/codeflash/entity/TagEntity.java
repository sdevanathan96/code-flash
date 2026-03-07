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

  @Column(name = "tag_type", nullable = false)
  private String tagType = "TOPIC";
}