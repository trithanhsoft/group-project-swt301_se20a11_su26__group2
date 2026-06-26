package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDepositRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 2000, message = "Minimum deposit amount is 2,000 VND")
    BigDecimal amount;
}
