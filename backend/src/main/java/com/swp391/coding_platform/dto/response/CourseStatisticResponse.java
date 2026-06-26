package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseStatisticResponse {
    private int totalEnrollments;
    private double averageRating;
    private int totalReviews;
    private BigDecimal totalRevenue;
    private double averageCompletionRate;
    private List<StudentProgressDto> students;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentProgressDto {
        private Integer userId;
        private String fullName;
        private String email;
        private String avatarUrl;
        private int completedLessons;
        private int totalLessons;
        private double completionPercentage;
    }
}
