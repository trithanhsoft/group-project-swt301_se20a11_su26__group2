package com.swp391.coding_platform.controller.problem;

import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.request.SubmitRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.service.problem.*;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemController {

    UserProblemService userProblemService;
    ProblemCommentService problemCommentService;
    ProblemSubmissionService problemSubmissionService;

    // 1. Problem List API
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProblemListItemResponse>>> getProblems(
            @AuthenticationPrincipal Jwt jwt) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        List<ProblemListItemResponse> result = userProblemService.getProblems(userId);

        return ResponseEntity.ok(ApiResponse.<List<ProblemListItemResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get problems successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    // 2. Description API
    @GetMapping("/{id}/description")
    public ResponseEntity<ApiResponse<ProblemDescriptionResponse>> getProblemDescription(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(id, userId);

        return ResponseEntity.ok(ApiResponse.<ProblemDescriptionResponse>builder()
                .status(200)
                .code(1000)
                .message("Get problem description successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    // 3. Discussion APIs
    @GetMapping("/{id}/discussion")
    public ResponseEntity<ApiResponse<List<ProblemCommentResponse>>> getDiscussion(
            @PathVariable("id") Integer id) {

        List<ProblemCommentResponse> result = problemCommentService.getComments(id);

        return ResponseEntity.ok(ApiResponse.<List<ProblemCommentResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get discussion comments successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/discussion")
    public ResponseEntity<ApiResponse<ProblemCommentResponse>> addDiscussionComment(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommentRequest request) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        ProblemCommentResponse result = problemCommentService.addComment(id, userId, request);

        return ResponseEntity.ok(ApiResponse.<ProblemCommentResponse>builder()
                .status(200)
                .code(1000)
                .message("Add discussion comment successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    // 4. Solution API
    @GetMapping("/{id}/solution")
    public ResponseEntity<ApiResponse<ProblemSolutionResponse>> getProblemSolution(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        ProblemSolutionResponse result = userProblemService.getProblemSolution(id, userId);

        return ResponseEntity.ok(ApiResponse.<ProblemSolutionResponse>builder()
                .status(200)
                .code(1000)
                .message("Get problem solution successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<ApiResponse<List<ProblemSubmissionResponse>>> getSubmissions(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt) {

        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        List<ProblemSubmissionResponse> result = problemSubmissionService.getSubmissions(id, userId);

        return ResponseEntity.ok(ApiResponse.<List<ProblemSubmissionResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get submissions successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
