package com.swp391.coding_platform.entity.instructor;

import com.swp391.coding_platform.entity.enums.InstructorAppStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
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
@Table(name = "instructor_applications", schema = "public")
public class InstructorApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Column(name = "cv_url", nullable = false)
    String cvUrl;

    @Column(name = "introduction", nullable = false, columnDefinition = "TEXT")
    String introduction;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    InstructorAppStatus status = InstructorAppStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    String adminNote;

    @Builder.Default
    @Column(name = "ai_score")
    Integer aiScore = 0;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    String aiSummary;

    @Column(name = "ai_specialization")
    String aiSpecialization;

    @Column(name = "ai_technologies", columnDefinition = "TEXT")
    String aiTechnologies;

    @Column(name = "ai_experience_years")
    Double aiExperienceYears;

    @Column(name = "ai_strengths", columnDefinition = "TEXT")
    String aiStrengths;

    @Column(name = "ai_weaknesses", columnDefinition = "TEXT")
    String aiWeaknesses;

    @Column(name = "ai_recommendation")
    String aiRecommendation;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
