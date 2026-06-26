package com.swp391.coding_platform.dto.response;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestScoreboardResponse {
    private Integer contestId;
    private List<TeamRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamRow {
        private int rank;
        private int userId;
        private String name; // username
        private String displayName;
        private String affiliation;
        private int solved;
        private int totalAttempts;
        private int totalPenalty; // in minutes
        private Map<String, ProblemSummary> submissions; // Key: problem label ("A", "B"...)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemSummary {
        private String time; // H:MM:SS
        private int penalty; // attempts before AC
        private String status; // 'first_solve', 'accepted', 'failed', 'unattempted'
    }
}
