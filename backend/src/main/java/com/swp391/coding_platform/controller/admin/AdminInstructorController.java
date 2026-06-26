package com.swp391.coding_platform.controller.admin;

import com.swp391.coding_platform.dto.request.ApproveApplicationRequest;
import com.swp391.coding_platform.dto.request.UpdateInstructorStatusRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.AdminInstructorResponse;
import com.swp391.coding_platform.dto.response.InstructorApplicationResponse;
import com.swp391.coding_platform.service.instructor.InstructorApplicationService;
import com.swp391.coding_platform.service.instructor.InstructorService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminInstructorController {

    InstructorService instructorService;
    InstructorApplicationService applicationService;

    @GetMapping("/admin/instructors")
    public ResponseEntity<ApiResponse<List<AdminInstructorResponse>>> getInstructors() {
        List<AdminInstructorResponse> result = instructorService.getAllInstructorsForAdmin();

        return ResponseEntity.ok(ApiResponse.<List<AdminInstructorResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched all active instructors successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/admin/instructors/applications")
    public ResponseEntity<ApiResponse<List<InstructorApplicationResponse>>> getApplications() {
        List<InstructorApplicationResponse> result = applicationService.getApplications();

        return ResponseEntity.ok(ApiResponse.<List<InstructorApplicationResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched all instructor applications successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/admin/instructors/applications/{id}/approve")
    public ResponseEntity<ApiResponse<InstructorApplicationResponse>> approveApplication(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ApproveApplicationRequest request) {

        InstructorApplicationResponse result = applicationService.approveApplication(id, request);

        return ResponseEntity.ok(ApiResponse.<InstructorApplicationResponse>builder()
                .status(200)
                .code(1000)
                .message("Application has been processed successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/admin/instructors/{id}/status")
    public ResponseEntity<ApiResponse<AdminInstructorResponse>> updateInstructorStatus(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateInstructorStatusRequest request) {

        AdminInstructorResponse result = instructorService.updateInstructorStatus(id, request.getStatus());

        return ResponseEntity.ok(ApiResponse.<AdminInstructorResponse>builder()
                .status(200)
                .code(1000)
                .message("Instructor status updated successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
