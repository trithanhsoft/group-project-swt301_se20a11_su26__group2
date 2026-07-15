package com.swp391.coding_platform.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {
    private String jobId;
    private JobState status;
    private AiVisualizerResponse result;
    private String errorMessage;
}
