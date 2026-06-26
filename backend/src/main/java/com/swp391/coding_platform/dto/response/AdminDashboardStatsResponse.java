package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    private long totalRevenue;
    private long activeUsers;
    private long activeContests;
    private long totalCourses;
    private long totalInstructors;
    private long totalProblems;

    private List<MonthlyStat> financialChartData;
    private List<TopCategory> topCategories;
    private List<TopCourse> topCourses;
    private List<TopInstructor> topInstructors;
    private List<TopProblem> topProblems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String label; // e.g. "Jun 26"
        private long amount;  // revenue amount
        private long count;   // courses sold count
        private long usersCount; // new registrations count
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCategory {
        private String name;
        private long count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCourse {
        private String name;
        private String instructor;
        private long count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopInstructor {
        private String name;
        private long count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProblem {
        private String name;
        private String difficulty; // EASY, MEDIUM, HARD
        private long count;
        private String color;
    }
}
