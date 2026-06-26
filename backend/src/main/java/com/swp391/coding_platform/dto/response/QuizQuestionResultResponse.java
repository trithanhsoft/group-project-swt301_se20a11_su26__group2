package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionResultResponse {
    Integer questionId;
    String content;
    Integer selectedOptionId;
    Integer correctOptionId;
    Boolean isCorrect;
    List<QuizOptionResultResponse> options;
}
