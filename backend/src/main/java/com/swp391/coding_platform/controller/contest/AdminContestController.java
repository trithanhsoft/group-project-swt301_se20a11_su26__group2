package com.swp391.coding_platform.controller.contest;

import com.swp391.coding_platform.dto.request.AdminContestRequest;
import com.swp391.coding_platform.dto.request.AdminContestProblemRequest;
import com.swp391.coding_platform.dto.response.AdminContestResponse;
import com.swp391.coding_platform.dto.response.AdminContestProblemResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.service.contest.ContestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin/contests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminContestController {

    ContestService contestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminContestResponse>>> getAdminContests() {
        List<AdminContestResponse> result = contestService.getAdminContests();
        return ResponseEntity.ok(ApiResponse.<List<AdminContestResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin contests successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminContestResponse>> createContest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminContestRequest request) {

        Integer adminUserId = null;
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                adminUserId = idClaim.intValue();
            }
        }

        AdminContestResponse result = contestService.createAdminContest(request, adminUserId);
        return ResponseEntity.ok(ApiResponse.<AdminContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Created contest successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminContestResponse>> getContestById(@PathVariable Integer id) {
        AdminContestResponse result = contestService.getAdminContestById(id);
        return ResponseEntity.ok(ApiResponse.<AdminContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Fetched contest details successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminContestResponse>> updateContest(
            @PathVariable Integer id,
            @Valid @RequestBody AdminContestRequest request) {
        AdminContestResponse result = contestService.updateAdminContest(id, request);
        return ResponseEntity.ok(ApiResponse.<AdminContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Updated contest successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContest(@PathVariable Integer id) {
        contestService.deleteAdminContest(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Deleted/Cancelled contest successfully")
                .timestamp(Instant.now().toString())
                .build());
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<AdminContestResponse>> publishContest(@PathVariable Integer id) {
        AdminContestResponse result = contestService.publishAdminContest(id);
        return ResponseEntity.ok(ApiResponse.<AdminContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Published contest successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<AdminContestResponse>> restoreContest(@PathVariable Integer id) {
        AdminContestResponse result = contestService.restoreAdminContest(id);
        return ResponseEntity.ok(ApiResponse.<AdminContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Restored contest successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteContest(@PathVariable Integer id) {
        contestService.hardDeleteAdminContest(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Permanently deleted contest successfully")
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/problems")
    public ResponseEntity<ApiResponse<List<AdminContestProblemResponse>>> getContestProblems(@PathVariable Integer id) {
        List<AdminContestProblemResponse> result = contestService.getAdminContestProblems(id);
        return ResponseEntity.ok(ApiResponse.<List<AdminContestProblemResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched contest problems successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/problems")
    public ResponseEntity<ApiResponse<Void>> addProblemToContest(
            @PathVariable Integer id,
            @Valid @RequestBody AdminContestProblemRequest request) {
        contestService.addProblemToContest(id, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Added problem to contest successfully")
                .timestamp(Instant.now().toString())
                .build());
    }

    @DeleteMapping("/{id}/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> removeProblemFromContest(
            @PathVariable Integer id,
            @PathVariable Integer problemId) {
        contestService.removeProblemFromContest(id, problemId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Removed problem from contest successfully")
                .timestamp(Instant.now().toString())
                .build());
    }
}
