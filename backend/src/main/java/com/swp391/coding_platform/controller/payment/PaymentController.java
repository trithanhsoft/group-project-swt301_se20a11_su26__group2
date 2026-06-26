package com.swp391.coding_platform.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swp391.coding_platform.dto.request.PaymentDepositRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.PaymentDepositResponse;
import com.swp391.coding_platform.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/payment/deposit")
    public ResponseEntity<ApiResponse<PaymentDepositResponse>> createDeposit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentDepositRequest request) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }
        PaymentDepositResponse response = paymentService.createDepositPayment(userId, request);

        return ResponseEntity.ok(ApiResponse.<PaymentDepositResponse>builder()
                .status(200)
                .code(1000)
                .message("Payment link created successfully")
                .result(response)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/payment/webhook")
    public ResponseEntity<ObjectNode> handleWebhook(@RequestBody ObjectNode payload) {
        log.info("Received PayOS Webhook");
        try {
            paymentService.handlePayOSWebhook(payload);
        } catch (Exception e) {
            log.error("PayOS Webhook Processing Error (Continuing to return 200 OK to PayOS): ", e);
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/balance")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getBalance(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        var balance = paymentService.getUserBalance(userId);

        return ResponseEntity.ok(ApiResponse.<java.math.BigDecimal>builder()
                .code(1000)
                .message("Get user balance successfully")
                .result(balance)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/payment/cancel/{transactionCode}")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String transactionCode) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }
        
        paymentService.cancelPayment(userId, transactionCode);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Payment cancelled successfully")
                .timestamp(Instant.now().toString())
                .build());
    }
}
