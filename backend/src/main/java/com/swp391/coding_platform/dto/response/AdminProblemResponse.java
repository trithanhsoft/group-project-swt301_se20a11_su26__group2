package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminProblemResponse {
    Integer id;
    String title;
    String description;
    String inputDescription;
    String outputDescription;
    String constraints;
    String exampleInput;
    String exampleOutput;
    String hint;
    String problemScope;
    String difficulty;
    Boolean isActive;
    Integer createdBy;
    String createdAt;
    Integer totalTestcases;
    Integer timeLimitMs;
    Integer memoryLimitKb;
    Boolean isPublic;
    Double score;
    String solutions;
    Integer totalSubmissions;
    Integer acceptedSubmissions;
    java.util.List<String> tags;
    java.util.Map<String, String> starterTemplates;
}
