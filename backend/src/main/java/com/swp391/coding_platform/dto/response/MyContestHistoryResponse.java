package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyContestHistoryResponse {
    Integer id;
    String title;
    String startDate;
    String endDate;
    String status;
    Integer rank;
    Long totalParticipants;
    Integer problemsSolved;
    Integer score; // Total penalty in minutes
}
