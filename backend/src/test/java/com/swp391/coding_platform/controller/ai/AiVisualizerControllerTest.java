package com.swp391.coding_platform.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.ai.AiVisualizerRequest;
import com.swp391.coding_platform.dto.response.ai.AiVisualizerResponse;
import com.swp391.coding_platform.dto.response.ai.JobStatusResponse;
import com.swp391.coding_platform.service.ai.AiVisualizerAsyncService;
import com.swp391.coding_platform.service.ai.AiVisualizerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AiVisualizerController.class)

public class AiVisualizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiVisualizerAsyncService asyncService;

    @MockBean
    private AiVisualizerService aiVisualizerService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void testGenerateVisualizer_WithCache() throws Exception {
        AiVisualizerRequest request = new AiVisualizerRequest();
        request.setProblemId("prob123");
        request.setForceRegenerate(false);

        AiVisualizerResponse mockResponse = AiVisualizerResponse.builder()
                .fromCache(true)
                .build();

        when(aiVisualizerService.getCurrentUserId()).thenReturn("user1");
        when(aiVisualizerService.getCachedVisualizer(eq("prob123"), eq("user1"), eq(false)))
                .thenReturn(Optional.of(mockResponse));

        mockMvc.perform(post("/api/v1/ai/visualizer/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.message").value("Success (from cache)"));
    }

    @Test
    void testGenerateVisualizer_NoCache() throws Exception {
        AiVisualizerRequest request = new AiVisualizerRequest();
        request.setProblemId("prob123");
        request.setForceRegenerate(false);

        when(aiVisualizerService.getCurrentUserId()).thenReturn("user1");
        when(aiVisualizerService.getCachedVisualizer(eq("prob123"), eq("user1"), eq(false)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/ai/visualizer/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(2020))
                .andExpect(jsonPath("$.result.jobId").exists())
                .andExpect(jsonPath("$.result.fromCache").value(false));
    }

    @Test
    void testGetJobStatus() throws Exception {
        JobStatusResponse jobStatus = new JobStatusResponse();
        when(asyncService.getJobStatus("job123")).thenReturn(jobStatus);

        mockMvc.perform(get("/api/v1/ai/visualizer/status/job123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void testGetCachedVisualizer_Found() throws Exception {
        AiVisualizerResponse mockResponse = AiVisualizerResponse.builder().build();

        when(aiVisualizerService.getCurrentUserId()).thenReturn("user1");
        when(aiVisualizerService.getCachedVisualizer("prob123", "user1", false))
                .thenReturn(Optional.of(mockResponse));

        mockMvc.perform(get("/api/v1/ai/visualizer/prob123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.message").value("Success (from cache)"));
    }

    @Test
    void testGetCachedVisualizer_NotFound() throws Exception {
        when(aiVisualizerService.getCurrentUserId()).thenReturn("user1");
        when(aiVisualizerService.getCachedVisualizer("prob123", "user1", false))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ai/visualizer/prob123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.message").value("No cache found"));
    }
}
