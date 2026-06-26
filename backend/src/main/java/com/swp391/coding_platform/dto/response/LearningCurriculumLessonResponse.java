package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningCurriculumLessonResponse {
    Integer id;
    String title;
    Boolean isTrial;
    Integer orderIndex;
    String type;
    Boolean isCompleted;
    String status;
}
