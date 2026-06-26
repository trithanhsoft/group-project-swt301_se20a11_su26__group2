package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionResponse {
    Integer questionId;
    String content;
    Integer orderIndex;
    List<QuizOptionResponse> options;

    // Populated only when submitted = true
    Integer selectedOptionId;
    Integer correctOptionId;
    Boolean isCorrect;
}
