package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestProblemResponse {
    Integer problemId;
    String title;
    Integer orderIndex;
    String difficulty;
    Integer totalSubmission;
    Integer totalAccepted;
    String status; // "SOLVED", "FAILED", "UNATTEMPTED"
}
