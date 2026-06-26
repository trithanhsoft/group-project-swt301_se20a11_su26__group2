package com.swp391.coding_platform.dto.response;

import com.swp391.coding_platform.entity.enums.OjVerdict;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OjWebSocketMessage {
    Integer submissionId;
    Integer testcaseId;

    OjVerdict testcaseVerdict; // Kết quả của riêng Testcase này (AC, WA...)
    OjVerdict overallVerdict;  // Kết quả TỔNG của cả bài nộp

    Integer executionTimeMs;
    Integer memoryUsedKb;

    Integer totalTestcases;
    Integer processedTestcases;

    // Fields for non-contest mode
    String input;
    String expectedOutput;
    String actualOutput;
    String compileOutput;
}