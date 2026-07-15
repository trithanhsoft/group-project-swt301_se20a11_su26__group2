package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFinancialPayoutDetailsResponse {
    private String id;
    private String instructorName;
    private String instructorEmail;
    private BigDecimal amount;
    private String bankAccount;
    private String status;
    private Instant date;
}
