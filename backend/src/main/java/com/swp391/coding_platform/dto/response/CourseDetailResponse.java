package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseDetailResponse {
    Long id;
    String title;
    String thumbnailUrl;
    String shortDescription;
    String longDescription;
    String whatYouLearn;
    String courseHighlight;
    String technologyTool;
    String prerequisites;
    String targetAudience;
    String completionBenefits;
    BigDecimal price;
    Double averageRating;
    Integer totalReviews;
    Integer totalEnrolled;
    Integer totalLessons;
    Integer totalQuizzes;
    Integer totalVideos;
    Boolean enrolled;
    Integer progressPercentage;
    String instructorName;
    String instructorTitle;
    String instructorBio;
    String instructorAvatarUrl;
    String categoryName;
}
