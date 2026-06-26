package com.swp391.coding_platform.entity.course;

import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "courses", schema = "public")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    InstructorEntity instructor;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "thumbnail_url", length = 255)
    String thumbnailUrl;


    @Column(name = "short_description", nullable = false, length = 255)
    String shortDescription;

    @Column(name = "long_description", nullable = false, columnDefinition = "TEXT")
    String longDescription;

    @Column(name = "what_you_learn", columnDefinition = "TEXT")
    String whatYouLearn;

    @Column(name = "course_highlight", columnDefinition = "TEXT")
    String courseHighlight;

    @Column(name = "technology_tool", length = 255)
    String technologyTool;

    @Column(name = "prerequisites", columnDefinition = "TEXT")
    String prerequisites;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    String targetAudience;

    @Column(name = "completion_benefits", columnDefinition = "TEXT")
    String completionBenefits;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    CourseStatus status = CourseStatus.DRAFTS;

    @Builder.Default
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    BigDecimal price = BigDecimal.ZERO;

    @Column(name = "type", nullable = false, length = 50)
    String type;

    @Builder.Default
    @Column(name = "average_rating", nullable = false)
    Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "total_reviews", nullable = false)
    Integer totalReviews = 0;

    @Builder.Default
    @Column(name = "total_enrolled", nullable = false)
    Integer totalEnrolled = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @Builder.Default
    @Column(name = "total_lessons", nullable = false)
    Integer totalLessons = 0;

    @Builder.Default
    @Column(name = "total_quizzes", nullable = false)
    Integer totalQuizzes = 0;

    @Builder.Default
    @Column(name = "total_videos", nullable = false)
    Integer totalVideos = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_category_mappings",
        schema = "public",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    Set<CategoryEntity> categories;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    java.util.List<ChapterEntity> chapters;
}
