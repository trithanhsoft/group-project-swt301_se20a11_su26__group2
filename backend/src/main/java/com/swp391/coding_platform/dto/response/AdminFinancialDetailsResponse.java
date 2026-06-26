package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminFinancialDetailsResponse {
    List<OrderDetails> orders;
    List<AwardDetails> awards;
    List<SaleDetails> sales;
    List<MonthlyFinancialBreakdown> monthlyBreakdowns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderDetails {
        String id;
        String customerName;
        String customerEmail;
        String courses;
        Long grossAmount;
        Long instructorShare;
        Long platformCut;
        String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AwardDetails {
        String id;
        String userName;
        String userEmail;
        Long amount;
        String date;
        String referenceId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SaleDetails {
        String orderId;
        String courseTitle;
        String instructorName;
        String customerName;
        Long price;
        String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MonthlyFinancialBreakdown {
        String label;
        String datePrefix;
        Long gross;
        Long count;
        Long rewards;
        Long server;
        Long marketing;
        Long netProfit;
    }
}
