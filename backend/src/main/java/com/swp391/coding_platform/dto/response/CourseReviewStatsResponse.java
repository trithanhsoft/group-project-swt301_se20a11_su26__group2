package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseReviewStatsResponse {
    Double averageRating;
    Integer totalReviews;
    Map<Integer, Long> starDistribution;
    CourseReviewDto myReview;
    PageResponse<CourseReviewDto> reviews;
}
