package com.swp391.coding_platform.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVisualizerRequest {
    private String problemId;
    private String title;
    private String description;
    private String constraints;
    private String exampleInput;
    private String exampleOutput;
    private String hint;
    private String userInput;
    private boolean forceRegenerate;
}
