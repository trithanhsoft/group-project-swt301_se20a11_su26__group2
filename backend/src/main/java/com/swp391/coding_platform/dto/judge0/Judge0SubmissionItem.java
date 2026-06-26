package com.swp391.coding_platform.dto.judge0;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Judge0SubmissionItem {
    
    @JsonProperty("language_id")
    Integer languageId;

    @JsonProperty("source_code")
    String sourceCode;

    @JsonProperty("stdin")
    String stdin; // Input của testcase (lấy từ DB)

    @JsonProperty("expected_output")
    String expectedOutput; // Output mẫu của testcase (lấy từ DB)

    @JsonProperty("callback_url")
    String callbackUrl; // ĐỊA CHỈ WEBHOOK cực kỳ quan trọng

    @JsonProperty("cpu_time_limit")
    Double cpuTimeLimit;

    @JsonProperty("memory_limit")
    Integer memoryLimit;
}