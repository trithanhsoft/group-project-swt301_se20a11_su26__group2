package com.swp391.coding_platform.controller.admin;

import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse;
import com.swp391.coding_platform.dto.response.AdminDepositHistoryResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.service.admin.AdminDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    AdminDashboardService adminDashboardService;


    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResponse result = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.<AdminDashboardStatsResponse>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin dashboard statistics successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/recent-deposits")
    public ResponseEntity<ApiResponse<List<AdminDepositHistoryResponse>>> getRecentDeposits() {
        List<AdminDepositHistoryResponse> result = adminDashboardService.getRecentDeposits();
        return ResponseEntity.ok(ApiResponse.<List<AdminDepositHistoryResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin dashboard recent deposits successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/all-deposits")
    public ResponseEntity<ApiResponse<List<AdminDepositHistoryResponse>>> getAllDeposits() {
        List<AdminDepositHistoryResponse> result = adminDashboardService.getAllDeposits();
        return ResponseEntity.ok(ApiResponse.<List<AdminDepositHistoryResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched all admin dashboard deposits successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
