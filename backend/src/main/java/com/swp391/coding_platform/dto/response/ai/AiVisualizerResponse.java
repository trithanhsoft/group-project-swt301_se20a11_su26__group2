package com.swp391.coding_platform.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVisualizerResponse {
    private String detectedAlgorithm;
    private String timeComplexity;
    private String htmlContent;
    private String jobId;
    private boolean fromCache;
}
