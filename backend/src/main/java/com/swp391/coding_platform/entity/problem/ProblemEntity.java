package com.swp391.coding_platform.entity.problem;

import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "problems", schema = "public")
public class ProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    String description;

    @Column(name = "input_description", columnDefinition = "TEXT")
    String inputDescription;

    @Column(name = "output_description", columnDefinition = "TEXT")
    String outputDescription;

    @Column(name = "constraints", columnDefinition = "TEXT")
    String constraints;

    @Column(name = "example_input", columnDefinition = "TEXT")
    String exampleInput;

    @Column(name = "example_output", columnDefinition = "TEXT")
    String exampleOutput;

    @Column(name = "hint", columnDefinition = "TEXT")
    String hint;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_scope", nullable = false)
    ProblemScope problemScope;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "difficulty")
    ProblemDifficulty difficulty = ProblemDifficulty.MEDIUM;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    UserEntity createdBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @Builder.Default
    @Column(name = "total_testcase", nullable = false)
    Integer totalTestcase = 0;

    @Builder.Default
    @Column(name = "time_limit_ms", nullable = false)
    Integer timeLimitMs = 2000;

    @Builder.Default
    @Column(name = "memory_limit_kb", nullable = false)
    Integer memoryLimitKb = 128000;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    Boolean isPublic = false;

    @Builder.Default
    @Column(name = "total_submission")
    Integer totalSubmission = 0;

    @Builder.Default
    @Column(name = "total_accepted")
    Integer totalAccepted = 0;

    @Builder.Default
    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    BigDecimal score = new BigDecimal("100.00");

    @Column(name = "solutions", columnDefinition = "TEXT")
    String solutions;

    @Column(name = "starter_templates", columnDefinition = "TEXT")
    String starterTemplates;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    java.util.List<ProblemTestcaseEntity> testcases = new java.util.ArrayList<>();
}
