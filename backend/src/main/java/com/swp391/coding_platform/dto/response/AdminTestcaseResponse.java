package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminTestcaseResponse {
    Integer id;
    Integer problemId;
    String inputData;
    String expectedOutput;
    Integer orderIndex;
    String token;
}
