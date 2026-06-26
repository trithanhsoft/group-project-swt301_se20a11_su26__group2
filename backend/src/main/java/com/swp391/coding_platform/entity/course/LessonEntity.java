package com.swp391.coding_platform.entity.course;

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
@Table(name = "lessons", schema = "public")
public class LessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    ChapterEntity chapter;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "theory_content", columnDefinition = "TEXT")
    String theoryContent;

    @Column(name = "video_url", length = 255)
    String videoUrl;

    @Column(name = "is_trial", nullable = false)
    Boolean isTrial;

    @Column(name = "order_index", nullable = false)
    Integer orderIndex;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @Column(name = "text_audio", columnDefinition = "TEXT")
    String textAudio;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    com.swp391.coding_platform.entity.enums.LessonStatus status = com.swp391.coding_platform.entity.enums.LessonStatus.ACTIVE;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    java.util.List<LessonProblemEntity> lessonProblems;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    java.util.List<com.swp391.coding_platform.entity.course.QuizEntity> quizzes;
}
