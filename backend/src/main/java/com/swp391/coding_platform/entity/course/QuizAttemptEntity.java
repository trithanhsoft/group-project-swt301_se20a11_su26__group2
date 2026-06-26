package com.swp391.coding_platform.entity.course;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "quiz_attempts", schema = "public")
public class QuizAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "user_id", nullable = false)
    Integer userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    QuizEntity quiz;

    @Column(name = "total_question", nullable = false)
    Integer totalQuestion;

    @Column(name = "correct_question", nullable = false)
    Integer correctQuestion;

    @Column(name = "score", nullable = false)
    Double score;

    @Column(name = "submitted_at")
    Instant submittedAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "quizAttempt", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<QuizAttemptAnswerEntity> answers;
}
