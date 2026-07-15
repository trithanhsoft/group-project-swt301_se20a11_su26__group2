package com.swp391.coding_platform.service.ai;

import com.swp391.coding_platform.dto.request.ai.AiVisualizerRequest;
import com.swp391.coding_platform.dto.response.ai.AiVisualizerResponse;
import com.swp391.coding_platform.dto.response.ai.JobState;
import com.swp391.coding_platform.dto.response.ai.JobStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiVisualizerAsyncServiceTest {

    @Mock
    private AiVisualizerService aiVisualizerService;

    @InjectMocks
    private AiVisualizerAsyncService asyncService;

    @Test
    void processVisualizerJob_Success_UpdatesJobStatus() {
        String jobId = "job123";
        String userId = "user1";
        AiVisualizerRequest request = new AiVisualizerRequest();
        request.setProblemId("prob1");
        
        AiVisualizerResponse response = AiVisualizerResponse.builder().build();

        when(aiVisualizerService.generateVisualizer(request, userId)).thenReturn(response);

        asyncService.processVisualizerJob(jobId, request, userId);

        JobStatusResponse status = asyncService.getJobStatus(jobId);
        assertEquals(JobState.SUCCESS, status.getStatus());
        assertEquals(jobId, status.getJobId());
        assertEquals(response, status.getResult());
        verify(aiVisualizerService, times(1)).generateVisualizer(request, userId);
    }

    @Test
    void processVisualizerJob_Exception_UpdatesJobStatusToFailed() {
        String jobId = "job456";
        String userId = "user1";
        AiVisualizerRequest request = new AiVisualizerRequest();
        request.setProblemId("prob2");

        when(aiVisualizerService.generateVisualizer(request, userId)).thenThrow(new RuntimeException("429 Too Many Requests"));

        asyncService.processVisualizerJob(jobId, request, userId);

        JobStatusResponse status = asyncService.getJobStatus(jobId);
        assertEquals(JobState.FAILED, status.getStatus());
        assertEquals(jobId, status.getJobId());
        assertEquals("The AI service is currently overloaded or under maintenance due to high traffic. Please try again in a few minutes.", status.getErrorMessage());
    }

    @Test
    void getJobStatus_UnknownJob_ReturnsPending() {
        JobStatusResponse status = asyncService.getJobStatus("unknown_job");
        assertEquals(JobState.PENDING, status.getStatus());
        assertEquals("unknown_job", status.getJobId());
    }
}
