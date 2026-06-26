package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminContestProblemResponse {
    Integer problemId;
    String title;
    String difficulty; // EASY, MEDIUM, HARD
    Integer orderIndex;
    Double score;
}
