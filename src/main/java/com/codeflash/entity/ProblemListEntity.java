package com.codeflash.entity;

import com.codeflash.domain.ListSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "name")
@ToString(of = {"id", "name", "source"})
@Entity
@Table(name = "problem_lists")
public class ProblemListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "list_source")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ListSource source;
    @Column(name = "favorite_id_hash")
    private String favoriteIdHash;
    @Column(name = "source_url", length = 500)
    private String sourceUrl;
}