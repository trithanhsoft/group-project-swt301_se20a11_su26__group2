package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCourseResponse {
    private String id;
    private String title;
    private String topic;
    private String price;
    private Integer studentsCount;
    private Double rating;
    private Integer reviewsCount;
    private String status;
    private String icon;
    private String gradient;
    private String description;
    private String thumbnailUrl;
}
