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
public class DashboardStatsResponse {
    private long enrolled;
    private long completedCourses;
    private long solvedPractice;
    private long totalPracticeProblems;
    private long ranking;
    private long totalUsers;
    private BigDecimal currentBalance;
}
