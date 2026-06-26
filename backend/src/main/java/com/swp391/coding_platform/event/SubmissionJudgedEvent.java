package com.swp391.coding_platform.event;

import java.time.Instant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionJudgedEvent {
    Integer submissionId;
    Integer userId;
    Integer contestId;
    Integer problemId;
    String verdict;
    Instant submitTime;
}
