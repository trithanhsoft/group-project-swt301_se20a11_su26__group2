package com.swp391.coding_platform.dto.response;

import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactionStatisticResponse {
    Instant date;
    String transactionCode;
    PaymentType type;
    BigDecimal amount;
    StatusTransaction status;
}
