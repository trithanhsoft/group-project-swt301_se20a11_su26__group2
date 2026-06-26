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
public class AdminTestcaseRequest {
    Integer problemId;
    @NotBlank
    String inputData;
    @NotBlank
    String expectedOutput;
    @NotNull
    Integer orderIndex;
}
