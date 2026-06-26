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
public class CourseListItemResponse {

    Long id;

    String title;

    String thumbnailUrl;

    String shortDescription;

    BigDecimal price;

    Double averageRating;

    Long totalReviews;

    Long totalEnrolled;

    Boolean enrolled;

    Integer progressPercentage;

    String instructorName;

}

