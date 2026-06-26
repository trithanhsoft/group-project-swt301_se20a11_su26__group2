package com.swp391.coding_platform.dto.response;

import com.swp391.coding_platform.entity.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCheckoutResponse {

    Integer orderId;
    BigDecimal totalAmount;
    OrderStatus status;

}
