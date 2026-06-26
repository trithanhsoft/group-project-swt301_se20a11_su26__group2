package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse {
    Integer id;
    String name;
    String email;
    String registerDate;
    String status;
    BigDecimal balance;
    BigDecimal totalDeposited;
    BigDecimal totalPurchased;
    List<PurchasedCourseDto> purchasedCourses;
    Boolean isOnline;
    String lockReason;
    String lockAppeal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PurchasedCourseDto {
        String id;
        String title;
        BigDecimal price;
        String date;
    }
}
