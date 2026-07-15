package com.swp391.coding_platform.controller.problem;

import com.swp391.coding_platform.dto.request.AdminProblemRequest;
import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.AdminProblemResponse;
import com.swp391.coding_platform.dto.response.AdminTestcaseResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.ProblemTagResponse;
import com.swp391.coding_platform.service.problem.AdminProblemService;
import com.swp391.coding_platform.service.problem.UserProblemService;
import com.swp391.coding_platform.service.problem.ProblemTestcaseService;
import com.swp391.coding_platform.service.problem.ProblemTagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProblemController {

    AdminProblemService adminProblemService;
    ProblemTagService problemTagService;
    ProblemTestcaseService problemTestcaseService;
    UserProblemService userProblemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminProblemResponse>>> getProblems() {
        List<AdminProblemResponse> result = adminProblemService.getAdminProblems();
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
        List<ProblemTagResponse> result = problemTagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.<List<ProblemTagResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched all tags successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminProblemResponse>> createProblem(
            @RequestBody AdminProblemRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Integer adminUserId = Math.toIntExact(jwt.getClaim("userId"));

        AdminProblemResponse result = adminProblemService.createAdminProblem(request, adminUserId);
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
            @RequestBody AdminProblemRequest request) {

        AdminProblemResponse result = adminProblemService.updateAdminProblem(id, request);
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
        adminProblemService.deleteAdminProblem(id);
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
        AdminProblemResponse result = adminProblemService.updateAdminProblemScope(id, problemScope);
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
        AdminProblemResponse result = adminProblemService.updateAdminProblemPublicStatus(id, isPublic);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Updated problem public status successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
    @PostMapping("/{id}/rollback/{versionId}")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> rollbackProblem(
            @PathVariable Integer id,
            @PathVariable Integer versionId) {

        AdminProblemResponse result = adminProblemService.rollbackAdminProblem(id, versionId);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .code(1000)
                .message("Problem rolled back successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> activateProblem(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {

        Integer totalTestcases = body.get("totalTestcases");
        AdminProblemResponse result = adminProblemService.activateAdminProblem(id, totalTestcases);
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

        List<AdminTestcaseResponse> result = problemTestcaseService.getProblemTestcases(id);
        return ResponseEntity.ok(ApiResponse.<List<AdminTestcaseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched problem testcases successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/testcases")
    public ResponseEntity<ApiResponse<List<AdminTestcaseResponse>>> saveProblemTestcases(
            @PathVariable Integer id,
            @RequestBody List<AdminTestcaseRequest> requests) {

        List<AdminTestcaseResponse> result = problemTestcaseService.saveProblemTestcases(id, requests);
        return ResponseEntity.ok(ApiResponse.<List<AdminTestcaseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Saved problem testcases successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<com.swp391.coding_platform.dto.response.ProblemVersionResponse>>> getProblemVersions(
            @PathVariable Integer id) {

        List<com.swp391.coding_platform.dto.response.ProblemVersionResponse> result = adminProblemService.getProblemVersions(id);
        return ResponseEntity.ok(ApiResponse.<List<com.swp391.coding_platform.dto.response.ProblemVersionResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched problem versions successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }


    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> cloneProblem(
            @PathVariable Integer id) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Integer adminUserId = Math.toIntExact(jwt.getClaim("userId"));

        AdminProblemResponse response = adminProblemService.cloneProblem(id, adminUserId);
        return ResponseEntity.ok(ApiResponse.<AdminProblemResponse>builder()
                .status(200)
                .message("Problem cloned successfully")
                .result(response)
                .build());
    }
}