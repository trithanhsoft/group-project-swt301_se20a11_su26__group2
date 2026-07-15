package com.swp391.coding_platform.controller.contest;

import com.swp391.coding_platform.dto.request.ContestSearchRequest;
import com.swp391.coding_platform.dto.request.ContestRegisterRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.ContestProblemResponse;
import com.swp391.coding_platform.dto.response.ContestResponse;
import com.swp391.coding_platform.dto.response.ContestUserStatsResponse;
import com.swp391.coding_platform.dto.response.ContestSubmissionResponse;
import com.swp391.coding_platform.dto.response.ContestProblemDetailResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.service.contest.ContestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/contests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestController {

    ContestService contestService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContestResponse>>> getContests(
            @AuthenticationPrincipal Jwt jwt,
            @jakarta.validation.Valid ContestSearchRequest request) {

        Integer userId = getUserId(jwt);
        PageResponse<ContestResponse> result = contestService.getContests(request, userId);

        return ResponseEntity.ok(ApiResponse.<PageResponse<ContestResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get contests list successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/banner")
    public ResponseEntity<ApiResponse<ContestResponse>> getBannerContest(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserId(jwt);
        ContestResponse result = contestService.getBannerContest(userId);

        return ResponseEntity.ok(ApiResponse.<ContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Get banner contest successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/user-stats")
    public ResponseEntity<ApiResponse<ContestUserStatsResponse>> getUserStats(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserId(jwt);
        ContestUserStatsResponse result = contestService.getUserStats(userId);

        return ResponseEntity.ok(ApiResponse.<ContestUserStatsResponse>builder()
                .status(200)
                .code(1000)
                .message("Get contest user stats successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/my-stats")
    public ResponseEntity<ApiResponse<com.swp391.coding_platform.dto.response.MyContestStatsResponse>> getMyContestStats(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserId(jwt);
        com.swp391.coding_platform.dto.response.MyContestStatsResponse result = contestService.getMyContestStats(userId);

        return ResponseEntity.ok(ApiResponse.<com.swp391.coding_platform.dto.response.MyContestStatsResponse>builder()
                .status(200)
                .code(1000)
                .message("Get user contest statistics successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<com.swp391.coding_platform.dto.response.MyContestHistoryResponse>>> getMyContestHistory(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = getUserId(jwt);
        List<com.swp391.coding_platform.dto.response.MyContestHistoryResponse> result = contestService.getMyContestHistory(userId);

        return ResponseEntity.ok(ApiResponse.<List<com.swp391.coding_platform.dto.response.MyContestHistoryResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get user contest history successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ApiResponse<ContestResponse>> getContestById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("contestId") Integer contestId) {

        Integer userId = getUserId(jwt);
        ContestResponse result = contestService.getContestById(contestId, userId);

        return ResponseEntity.ok(ApiResponse.<ContestResponse>builder()
                .status(200)
                .code(1000)
                .message("Get contest by id successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{contestId}/register")
    public ResponseEntity<ApiResponse<Void>> registerForContest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("contestId") Integer contestId,
            @RequestBody(required = false) ContestRegisterRequest request) {

        Integer userId = getUserId(jwt);

        contestService.registerForContest(contestId, userId, request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Registered for contest successfully")
                .build());
    }

    @GetMapping("/{contestId}/problems")
    public ResponseEntity<ApiResponse<List<ContestProblemResponse>>> getContestProblems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("contestId") Integer contestId,
            Authentication authentication) {

        Integer userId = getUserId(jwt);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        List<ContestProblemResponse> result = contestService.getContestProblems(contestId, userId, isAdmin);

        return ResponseEntity.ok(ApiResponse.<List<ContestProblemResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get contest problems successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{contestId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<ContestProblemDetailResponse>> getContestProblemDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("contestId") Integer contestId,
            @PathVariable("problemId") Integer problemId,
            Authentication authentication) {
        Integer userId = getUserId(jwt);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
                
        ContestProblemDetailResponse result = contestService.getContestProblemDetail(contestId, problemId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.<ContestProblemDetailResponse>builder()
                .status(200)
                .code(1000)
                .message("Get contest problem detail successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{contestId}/submissions")
    public ResponseEntity<ApiResponse<List<ContestSubmissionResponse>>> getContestSubmissions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("contestId") Integer contestId,
            Authentication authentication) {

        Integer userId = getUserId(jwt);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        List<ContestSubmissionResponse> result = contestService.getContestSubmissions(contestId, userId, isAdmin);

        return ResponseEntity.ok(ApiResponse.<List<ContestSubmissionResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get contest submissions successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    private Integer getUserId(Jwt jwt) {
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                return idClaim.intValue();
            }
        }
        return null;
    }
}
