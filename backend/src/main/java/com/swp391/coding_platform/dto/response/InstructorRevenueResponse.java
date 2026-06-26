package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorRevenueResponse {
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalNetRevenue;
    private BigDecimal totalActualTakeHome;
    private List<SalesHistoryItem> salesHistory;
    private List<RecentRegistration> recentRegistrations;
    private List<PayoutHistoryItem> payoutHistory;
    private List<CourseBreakdownItem> courseBreakdown;
    private List<MonthlyChartItem> monthlyChartData;
    private List<CourseRegistrationsItem> courseRegistrations;
    private Integer totalTrendRegistrations;
}
