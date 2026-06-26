package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminFinancialTopCourseResponse {
    String name;
    String tutor;
    Long sold;
    Long gross;
    Long payout;
    Long plat;
}
