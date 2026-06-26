package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminContestProblemRequest {

    @NotNull(message = "Problem ID cannot be null")
    Integer problemId;

    @NotNull(message = "Order index cannot be null")
    Integer orderIndex;
}
