package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitRequest {
    @NotBlank(message = "Language is required")
    String language;

    @NotBlank(message = "Source code is required")
    String sourceCode;
}
