package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionStatisticResponse {
    long totalSubmissions;
    long totalAccepted;
    long totalWrongAnswer;
    long totalTimeLimitExceeded;
    long totalMemoryLimitExceeded;
}
