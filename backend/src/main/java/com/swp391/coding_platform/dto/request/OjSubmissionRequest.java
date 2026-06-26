package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OjSubmissionRequest {
    @NotNull(message = "OJ_PROBLEM_ID_REQUIRED")
    Integer problemId;

    Integer lessonId;
    Integer contestId;

    @NotNull(message = "OJ_LANGUAGE_ID_REQUIRED")
    Integer languageId;

    @NotBlank(message = "OJ_SOURCE_CODE_EMPTY")
    String sourceCode;
}