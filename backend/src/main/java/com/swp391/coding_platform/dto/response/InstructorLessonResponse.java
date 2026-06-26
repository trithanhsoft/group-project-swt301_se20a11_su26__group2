package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructorLessonResponse {
    Integer id;
    String title;
    Boolean isTrial;
    Integer orderIndex;
    String videoUrl;
    String theoryContent;
    java.util.List<InstructorExerciseResponse> exercises;
    java.util.List<InstructorQuizResponse> quizzes;
    String status;
}
