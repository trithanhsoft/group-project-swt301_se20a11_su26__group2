package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminContestResponse {
    Integer id;
    String title;
    String description;
    String scoringRule;
    Instant startTime;
    Instant endTime;
    Integer durations; // in minutes
    String status; // UPCOMING, RUNNING, ENDED, CANCELLED
    String creatorName;
    Boolean isPrivate;
    Integer participantCount;
    Integer problemCount;
    Integer submissionCount;
    Double averageScore;
    Boolean isDeleted;
    String databaseStatus;
}
