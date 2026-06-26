package com.swp391.coding_platform.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseItemResponse {
    Integer courseId;
    String courseTitle;
    String instructorName;
    BigDecimal priceAtPurchase;
}
