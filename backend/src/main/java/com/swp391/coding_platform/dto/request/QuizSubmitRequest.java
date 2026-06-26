package com.swp391.coding_platform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmitRequest {
    
    @NotNull(message = "Answers list cannot be null")
    @Valid
    List<AnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        @NotNull(message = "Question ID cannot be null")
        Integer questionId;
        
        Integer selectedOptionId; // Nullable if the student skipped the question
    }
}
