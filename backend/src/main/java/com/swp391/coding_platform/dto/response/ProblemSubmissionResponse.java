package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemSubmissionResponse {
    Integer problemId;
    String problemTitle;
    String status;
    String lang;
    String runtime;
    String memory;
    String time;
    String statusClass;
}
