package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseResponse {
    private Long id;
    private Integer instructorId;
    private String instructorName;
    private String instructorAvatarUrl;
    private String title;
    private String thumbnailUrl;
    private String shortDescription;
    private String longDescription;
    private String status;
    private BigDecimal price;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalEnrolled;
    private Integer totalLessons;
    private Integer totalQuizzes;
    private Integer totalVideos;
    private Integer totalChapters;
}
