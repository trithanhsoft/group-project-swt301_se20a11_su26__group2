package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDepositResponse {
    String checkoutUrl;
    String transactionCode;
    String qrCode;
    String accountNumber;
    String accountName;
    String bin;
}
