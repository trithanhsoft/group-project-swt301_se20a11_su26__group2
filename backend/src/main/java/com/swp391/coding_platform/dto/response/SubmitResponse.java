package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitResponse {
    String verdict;          // e.g. "ACCEPTED", "WRONG_ANSWER"
    Double runtime;          // in ms
    Integer memory;          // in KB
    Integer passedTestcases;
    Integer totalTestcases;
}
