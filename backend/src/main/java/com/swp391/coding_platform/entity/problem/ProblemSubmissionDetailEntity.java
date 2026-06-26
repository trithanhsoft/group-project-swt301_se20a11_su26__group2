package com.swp391.coding_platform.entity.problem;

import com.swp391.coding_platform.entity.enums.OjVerdict;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "problem_submission_details", schema = "public")
public class ProblemSubmissionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    ProblemSubmissionEntity submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "testcase_id", nullable = false)
    ProblemTestcaseEntity testcase;

    @Column(name = "token", length = 255)
    String token;

    @Column(name = "execution_time")
    Integer executionTime;

    @Column(name = "memory_used")
    Integer memoryUsed;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "verdict")
    OjVerdict verdict = OjVerdict.PENDING;

    @Column(name = "stdout", columnDefinition = "TEXT")
    String stdout;

    @Column(name = "stderr", columnDefinition = "TEXT")
    String stderr;

    @Column(name = "compile_output", columnDefinition = "TEXT")
    String compileOutput;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();
}
