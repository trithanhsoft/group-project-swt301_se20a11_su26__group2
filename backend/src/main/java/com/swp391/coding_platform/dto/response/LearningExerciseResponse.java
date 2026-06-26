package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningExerciseResponse {
    Integer id;
    String name;
    String difficulty;
    String difficultyClass;
    Integer submissions;
    Boolean completed;
}
