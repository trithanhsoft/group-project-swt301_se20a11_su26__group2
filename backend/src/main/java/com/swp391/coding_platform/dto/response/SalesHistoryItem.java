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
public class SalesHistoryItem {
    private String id;
    private String studentName;
    private String courseId;
    private String courseTitle;
    private BigDecimal amount;
    private String timestamp;
}
