package com.swp391.coding_platform.entity.problem;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "problem_submissions", schema = "public")
public class ProblemSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    ProblemEntity problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_version_id", nullable = false)
    ProblemVersionEntity problemVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    LessonEntity lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    ContestEntity contest;

    @Column(name = "language_id", nullable = false)
    Integer languageId;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    String sourceCode;

    @Column(name = "execution_time")
    Integer executionTime;

    @Column(name = "memory_used")
    Integer memoryUsed;

    @Column(name = "score", precision = 10, scale = 2)
    BigDecimal score;

    @Builder.Default
    @Column(name = "submitted_at", nullable = false)
    Instant submittedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "verdict")
    OjVerdict verdict = OjVerdict.PENDING;

    @Builder.Default
    @Column(name = "is_plagiarized")
    Boolean isPlagiarized = false;
}
