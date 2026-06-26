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
public class PayoutHistoryItem {
    private String id;
    private String payoutPeriod;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String status;
    private String transactionReference;
    private String adminNote;
}
