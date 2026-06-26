package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestUserStatsResponse {
    String displayName;
    String avatarUrl;
    Integer score;
    Integer rank;
    Long totalUsers;
    Long contestsCount;
    Integer avgAccuracy;
}
