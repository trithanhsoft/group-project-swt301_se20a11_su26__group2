package com.swp391.coding_platform.dto.response;

import com.swp391.coding_platform.entity.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseHistoryResponse {
    Integer orderId;
    BigDecimal totalAmount;
    OrderStatus status;
    Instant purchaseDate;
    List<PurchaseItemResponse> items;
}
