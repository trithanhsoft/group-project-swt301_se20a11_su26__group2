package com.swp391.coding_platform.controller.admin;

import com.swp391.coding_platform.dto.response.AdminFinancialMonthlyRecordResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialTopCourseResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.service.admin.AdminFinancialService;
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
@RequestMapping("/admin/financial")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminFinancialController {

    AdminFinancialService adminFinancialService;

    @GetMapping("/monthly-records")
    public ResponseEntity<ApiResponse<List<AdminFinancialMonthlyRecordResponse>>> getMonthlyRecords() {
        List<AdminFinancialMonthlyRecordResponse> result = adminFinancialService.getMonthlyFinancialRecords();
        return ResponseEntity.ok(ApiResponse.<List<AdminFinancialMonthlyRecordResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin financial monthly records successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/top-courses")
    public ResponseEntity<ApiResponse<List<AdminFinancialTopCourseResponse>>> getTopCourses() {
        List<AdminFinancialTopCourseResponse> result = adminFinancialService.getTopRevenueCoursesData();
        return ResponseEntity.ok(ApiResponse.<List<AdminFinancialTopCourseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin top revenue courses successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<com.swp391.coding_platform.dto.response.AdminFinancialDetailsResponse>> getFinancialDetails() {
        com.swp391.coding_platform.dto.response.AdminFinancialDetailsResponse result = adminFinancialService.getFinancialDetails();
        return ResponseEntity.ok(ApiResponse.<com.swp391.coding_platform.dto.response.AdminFinancialDetailsResponse>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin financial audit details successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
