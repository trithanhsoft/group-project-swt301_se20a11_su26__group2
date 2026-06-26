package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminProblemRequest {
    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Description is required")
    String description;

    String inputDescription;
    String outputDescription;
    String constraints;
    String exampleInput;
    String exampleOutput;
    String hint;
    String problemScope;
    String difficulty;
    Integer totalTestcases;
    Integer timeLimitMs;
    Integer memoryLimitKb;
    Boolean isPublic;
    Double score;
    String solutions;
    java.util.List<String> tags;
    java.util.Map<String, String> starterTemplates;
}
