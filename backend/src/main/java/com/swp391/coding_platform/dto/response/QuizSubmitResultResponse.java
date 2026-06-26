package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmitResultResponse {
    Integer attemptId;
    Integer totalQuestion;
    Integer correctQuestion;
    Double score;
    Instant submittedAt;
    List<QuizQuestionResultResponse> results;
}
