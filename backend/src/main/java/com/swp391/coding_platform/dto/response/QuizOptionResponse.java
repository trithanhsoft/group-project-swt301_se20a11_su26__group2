package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizOptionResponse {
    Integer optionId;
    String content;
    Integer orderIndex;
    
    // Populated only when submitted = true
    Boolean isCorrect;
}
