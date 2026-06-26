package com.swp391.coding_platform.dto.response;

import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructorExerciseResponse {
    Integer id;
    String title;
    ProblemDifficulty difficulty;
    String description;
    String inputDesc;
    String outputDesc;
    String constraints;
    String exampleInput;
    String exampleOutput;
    String hint;
    Integer score;
    Integer timeLimit;
    Integer memoryLimit;
    String initialCode;
    String solutionCode;
    java.util.List<InstructorTestcaseResponse> testCases;
    Integer orderIndex;
}
