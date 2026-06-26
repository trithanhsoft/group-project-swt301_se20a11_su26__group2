package com.swp391.coding_platform.entity.progress;

import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
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
@Table(
        name = "completed_lessons_count",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_completed_lessons_count_user_course",
                        columnNames = {"user_id", "course_id"}
                )
        }
)
public class CompletedLessonsCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    CourseEntity course;

    @Builder.Default
    @Column(name = "completed_lessons_count", nullable = false)
    Integer completedLessonsCount = 0;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (completedLessonsCount == null) {
            completedLessonsCount = 0;
        }

        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
