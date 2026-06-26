package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningLessonResponse {
    Integer id;
    String title;
    String videoUrl;
    String theoryContent;
    String sourceCode;
    List<ProblemListItemResponse> problems;
    QuizDetailResponse quiz;
    java.util.List<LearningExerciseResponse> exercises;
    String status;
}
