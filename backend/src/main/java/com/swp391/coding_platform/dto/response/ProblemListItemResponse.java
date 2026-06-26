package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemListItemResponse {
    Integer id;
    String title;
    String difficulty;
    List<String> tags;
    Integer score;
    Integer totalSubmission;
    Integer totalAccepted;
    Boolean isSolved;
    String status;
}
