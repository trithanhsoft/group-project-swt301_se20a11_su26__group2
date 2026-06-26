package com.swp391.coding_platform.controller.problem;

import com.swp391.coding_platform.dto.request.AdminProblemRequest;
import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.AdminProblemResponse;
import com.swp391.coding_platform.dto.response.AdminTestcaseResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.ProblemTagResponse;
import com.swp391.coding_platform.service.problem.ProblemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProblemController {

    ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminProblemResponse>>> getProblems() {
        List<AdminProblemResponse> result = problemService.getAdminProblems();
        return ResponseEntity.ok(ApiResponse.<List<AdminProblemResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin problems successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<ProblemTagResponse>>> getAllTags() {
        List<ProblemTagResponse> result = problemService.getAllTags();
        return ResponseEntity.ok(ApiResponse.<List<ProblemTagResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched tags successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminProblemResponse>> createProblem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminProblemRequest request) {

        Integer adminUserId = null;
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                adminUserId = idClaim.intValue();
            }
        }

        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }

        AdminProblemResponse result = problemService.createAdminProblem(request, adminUserId);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Created problem successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> updateProblem(
            @PathVariable Integer id,
            @Valid @RequestBody AdminProblemRequest request) {

        AdminProblemResponse result = problemService.updateAdminProblem(id, request);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Updated problem successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Integer id) {
        problemService.deleteAdminProblem(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Deleted problem successfully")
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/scope")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> updateProblemScope(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String problemScope = body.get("problemScope");
        AdminProblemResponse result = problemService.updateAdminProblemScope(id, problemScope);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Updated problem scope successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/public")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> updateProblemPublicStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {

        Boolean isPublic = body.get("isPublic");
        AdminProblemResponse result = problemService.updateAdminProblemPublicStatus(id, isPublic);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Updated problem public status successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> activateProblem(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {

        Integer totalTestcases = body.get("totalTestcases");
        AdminProblemResponse result = problemService.activateAdminProblem(id, totalTestcases);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Activated problem successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/testcases")
    public ResponseEntity<ApiResponse<List<AdminTestcaseResponse>>> getProblemTestcases(
            @PathVariable Integer id) {
        List<AdminTestcaseResponse> result = problemService.getProblemTestcases(id);
        return ResponseEntity.ok(ApiResponse.<List<AdminTestcaseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched testcases successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/testcases")
    public ResponseEntity<ApiResponse<List<AdminTestcaseResponse>>> saveProblemTestcases(
            @PathVariable Integer id,
            @Valid @RequestBody List<AdminTestcaseRequest> requests) {
        List<AdminTestcaseResponse> result = problemService.saveProblemTestcases(id, requests);
        return ResponseEntity.ok(ApiResponse.<List<AdminTestcaseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Saved testcases successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
