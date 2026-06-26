package com.swp391.coding_platform.controller.payment;


import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.request.OrderCheckoutRequest;
import com.swp391.coding_platform.dto.response.OrderCheckoutResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.PurchaseHistoryResponse;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.service.payment.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderCheckoutResponse>> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderCheckoutRequest request) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }
        log.info("User {} is checking out courses: {}", userId, request.getCourseIds());
        
        OrderCheckoutResponse response = orderService.createCheckout(userId, request);

        return ResponseEntity.ok(ApiResponse.<OrderCheckoutResponse>builder()
                .status(200)
                .code(1000)
                .message("Order checkout completed successfully")
                .result(response)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/purchase-history")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseHistoryResponse>>> getPurchaseHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (jwt == null || jwt.getClaim("userId") == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Integer userId = ((Number) jwt.getClaim("userId")).intValue();
        log.info("Fetching purchase history for user {}", userId);

        PageResponse<PurchaseHistoryResponse> response = orderService.getPurchaseHistory(userId, page, size);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PurchaseHistoryResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched purchase history successfully")
                .result(response)
                .timestamp(Instant.now().toString())
                .build());
    }

}
