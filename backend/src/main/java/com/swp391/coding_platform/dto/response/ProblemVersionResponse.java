package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemVersionResponse {
    Integer id;
    Integer problemId;
    Integer versionNumber;
    String title;
    String description;
    String inputDescription;
    String outputDescription;
    String constraints;
    String exampleInput;
    String exampleOutput;
    String hint;
    String difficulty;
    Integer timeLimitMs;
    Integer memoryLimitKb;
    String solutions;
    String createdAt;
    java.util.List<AdminTestcaseResponse> testcases;
}
