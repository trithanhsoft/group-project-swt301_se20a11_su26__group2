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
public class QuizDetailResponse {
    Integer quizId;
    String title;
    
    // Status/Result of the user's latest attempt
    Boolean submitted;
    Double score;
    Integer totalQuestion;
    Integer correctQuestion;
    Instant submittedAt;

    List<QuizQuestionResponse> questions;
}
