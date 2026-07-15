package com.swp391.coding_platform.controller.ai;

import com.swp391.coding_platform.dto.request.ai.AiVisualizerRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.ai.AiVisualizerResponse;
import com.swp391.coding_platform.dto.response.ai.JobStatusResponse;
import com.swp391.coding_platform.service.ai.AiVisualizerAsyncService;
import com.swp391.coding_platform.service.ai.AiVisualizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/visualizer")
@RequiredArgsConstructor
public class AiVisualizerController {

    private final AiVisualizerAsyncService asyncService;
    private final AiVisualizerService aiVisualizerService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiVisualizerResponse>> generateVisualizer(@RequestBody AiVisualizerRequest request) {
        // Validate scope via Service
        aiVisualizerService.validateProblemScope(request.getProblemId());

        String userId = aiVisualizerService.getCurrentUserId();

        // 1. Check cache synchronously first for fast return
        Optional<AiVisualizerResponse> cached = aiVisualizerService.getCachedVisualizer(request.getProblemId(), userId, request.isForceRegenerate());
        if (cached.isPresent()) {
            return ResponseEntity.ok(ApiResponse.<AiVisualizerResponse>builder()
                    .status(HttpStatus.OK.value())
                    .code(2000)
                    .message("Success (from cache)")
                    .result(cached.get())
                    .timestamp(Instant.now().toString())
                    .build());
        }

        // 2. No cache, create job
        String jobId = UUID.randomUUID().toString();
        asyncService.processVisualizerJob(jobId, request, userId);

        // 3. Return 202 Accepted with jobId
        AiVisualizerResponse response = AiVisualizerResponse.builder()
                .jobId(jobId)
                .fromCache(false)
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.<AiVisualizerResponse>builder()
                .status(HttpStatus.ACCEPTED.value())
                .code(2020)
                .message("Processing request")
                .result(response)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(@PathVariable("jobId") String jobId) {
        return ResponseEntity.ok(ApiResponse.<JobStatusResponse>builder()
                .status(HttpStatus.OK.value())
                .code(2000)
                .message("Success")
                .result(asyncService.getJobStatus(jobId))
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<AiVisualizerResponse>> getCachedVisualizer(@PathVariable("problemId") String problemId) {
        String userId = aiVisualizerService.getCurrentUserId();

        Optional<AiVisualizerResponse> cached = aiVisualizerService.getCachedVisualizer(problemId, userId, false);
        
        if (cached.isPresent()) {
            return ResponseEntity.ok(ApiResponse.<AiVisualizerResponse>builder()
                    .status(HttpStatus.OK.value())
                    .code(2000)
                    .message("Success (from cache)")
                    .result(cached.get())
                    .timestamp(Instant.now().toString())
                    .build());
        } else {
            return ResponseEntity.ok(ApiResponse.<AiVisualizerResponse>builder()
                    .status(HttpStatus.OK.value())
                    .code(2000)
                    .message("No cache found")
                    .result(null)
                    .timestamp(Instant.now().toString())
                    .build());
        }
    }
}
