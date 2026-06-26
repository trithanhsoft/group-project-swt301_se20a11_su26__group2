package com.swp391.coding_platform.dto.moderation;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiEmbeddingResponse {
    private Embedding embedding;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding {
        private List<Double> values;
    }
}
