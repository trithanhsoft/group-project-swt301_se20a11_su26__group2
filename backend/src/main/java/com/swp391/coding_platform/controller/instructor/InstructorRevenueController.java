package com.swp391.coding_platform.controller.instructor;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.service.instructor.InstructorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/instructor/revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InstructorRevenueController {

    InstructorService instructorService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<InstructorRevenueSummary>> getRevenueSummary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "filter", defaultValue = "this-month") String filter,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        InstructorRevenueSummary result = instructorService.getRevenueSummary(userId, filter, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<InstructorRevenueSummary>builder()
                .status(200)
                .code(1000)
                .message("Fetched revenue summary successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/sales-history")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<SalesHistoryItem>>> getSalesHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "filter", defaultValue = "this-month") String filter,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<SalesHistoryItem> result = instructorService.getSalesHistory(userId, filter, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<List<SalesHistoryItem>>builder()
                .status(200)
                .code(1000)
                .message("Fetched sales history successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/recent-registrations")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<RecentRegistration>>> getRecentRegistrations(
            @AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<RecentRegistration> result = instructorService.getRecentRegistrations(userId);
        return ResponseEntity.ok(ApiResponse.<List<RecentRegistration>>builder()
                .status(200)
                .code(1000)
                .message("Fetched recent registrations successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/payout-history")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<PayoutHistoryItem>>> getPayoutHistory(
            @AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<PayoutHistoryItem> result = instructorService.getPayoutHistory(userId);
        return ResponseEntity.ok(ApiResponse.<List<PayoutHistoryItem>>builder()
                .status(200)
                .code(1000)
                .message("Fetched payout history successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/course-breakdown")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<CourseBreakdownItem>>> getCourseBreakdown(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "filter", defaultValue = "this-month") String filter,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<CourseBreakdownItem> result = instructorService.getCourseBreakdown(userId, filter, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<List<CourseBreakdownItem>>builder()
                .status(200)
                .code(1000)
                .message("Fetched course revenue breakdown successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/chart-data")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<MonthlyChartItem>>> getMonthlyChartData(
            @AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<MonthlyChartItem> result = instructorService.getMonthlyChartData(userId);
        return ResponseEntity.ok(ApiResponse.<List<MonthlyChartItem>>builder()
                .status(200)
                .code(1000)
                .message("Fetched monthly chart data successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/course-registrations")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<InstructorCourseRegistrationsResponse>> getCourseRegistrations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "trendTimeframe", defaultValue = "12m") String trendTimeframe) {
        Integer userId = getUserIdFromJwt(jwt);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }


        InstructorCourseRegistrationsResponse result = instructorService.getCourseRegistrations(userId, trendTimeframe);
        return ResponseEntity.ok(ApiResponse.<InstructorCourseRegistrationsResponse>builder()
                .status(200)
                .code(1000)
                .message("Fetched course registrations stats successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    private Integer getUserIdFromJwt(Jwt jwt) {
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                return idClaim.intValue();
            }
        }
        return null;
    }
}
