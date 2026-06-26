package com.swp391.coding_platform.dto.moderation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiEmbeddingRequest {
    private String model;
    private Content content;
    private Integer outputDimensionality; // Enforce matching vector size for pgvector

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    public static GeminiEmbeddingRequest of(String text) {
        return GeminiEmbeddingRequest.builder()
                .model("models/gemini-embedding-001")
                .content(Content.builder()
                        .parts(Collections.singletonList(
                            Part.builder().text(text).build()
                        ))
                        .build())
                .outputDimensionality(768) // Truncate output to 768 dimensions
                .build();
    }
}
