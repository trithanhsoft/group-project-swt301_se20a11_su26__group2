package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyContestStatsResponse {
    Long totalContests;
    Integer top1Count;
    Integer top2Count;
    Integer top3Count;
}
