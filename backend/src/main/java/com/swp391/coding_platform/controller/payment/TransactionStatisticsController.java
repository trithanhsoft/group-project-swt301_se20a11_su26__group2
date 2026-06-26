package com.swp391.coding_platform.controller.payment;

import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.PaymentTransactionStatisticResponse;
import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.service.payment.TransactionStatisticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionStatisticsController {

    TransactionStatisticsService transactionStatisticsService;

    @GetMapping("/wallet/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionStatisticResponse>>> getWalletTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(value = "type", required = false) TransactionType type) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        PageResponse<TransactionStatisticResponse> result = transactionStatisticsService
                .getTransactionStatistics(userId, type, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<TransactionStatisticResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get wallet transaction statistics successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/payment/transactions")
    public ResponseEntity<ApiResponse<PageResponse<PaymentTransactionStatisticResponse>>> getPaymentTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(value = "type", required = false) PaymentType type) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        PageResponse<PaymentTransactionStatisticResponse> result = transactionStatisticsService
                .getPaymentTransactionStatistics(userId, type, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PaymentTransactionStatisticResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get payment transaction statistics successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
