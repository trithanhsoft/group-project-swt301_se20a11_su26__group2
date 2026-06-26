package com.swp391.coding_platform.dto.message;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestRankingDbUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer contestId;
    private Integer userId;
    private int problemsSolved;
    private int totalPenaltyMinutes;

    // Additional fields for ContestProblemAttempt update
    private Integer problemId;
    private Boolean isSolved;
    private Integer solvedAtSeconds;
    private Integer failedAttemptsCount;
}
