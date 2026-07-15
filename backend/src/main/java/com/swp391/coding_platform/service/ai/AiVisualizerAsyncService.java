package com.swp391.coding_platform.service.ai;

import com.swp391.coding_platform.dto.request.ai.AiVisualizerRequest;
import com.swp391.coding_platform.dto.response.ai.AiVisualizerResponse;
import com.swp391.coding_platform.dto.response.ai.JobState;
import com.swp391.coding_platform.dto.response.ai.JobStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiVisualizerAsyncService {

    private final AiVisualizerService aiVisualizerService;
    
    // In-memory Job State (could be replaced by Redis for distributed scaling)
    private final ConcurrentHashMap<String, JobStatusResponse> jobStore = new ConcurrentHashMap<>();

    @Async
    public void processVisualizerJob(String jobId, AiVisualizerRequest request, String userId) {
        log.info("Starting async job {} for problem {}", jobId, request.getProblemId());
        
        jobStore.put(jobId, JobStatusResponse.builder()
                .jobId(jobId)
                .status(JobState.PROCESSING)
                .build());

        try {
            AiVisualizerResponse response = aiVisualizerService.generateVisualizer(request, userId);
            response.setJobId(jobId);
            
            jobStore.put(jobId, JobStatusResponse.builder()
                    .jobId(jobId)
                    .status(JobState.SUCCESS)
                    .result(response)
                    .build());
                    
            log.info("Async job {} completed successfully", jobId);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("Quota exceeded") || errorMsg.contains("RESOURCE_EXHAUSTED"))) {
                errorMsg = "The AI service is currently overloaded or under maintenance due to high traffic. Please try again in a few minutes.";
            }
            
            log.error("Async job {} failed: {}", jobId, e.getMessage());
            jobStore.put(jobId, JobStatusResponse.builder()
                    .jobId(jobId)
                    .status(JobState.FAILED)
                    .errorMessage(errorMsg)
                    .build());
        }
    }

    public JobStatusResponse getJobStatus(String jobId) {
        return jobStore.getOrDefault(jobId, JobStatusResponse.builder()
                .jobId(jobId)
                .status(JobState.PENDING)
                .build());
    }
}
