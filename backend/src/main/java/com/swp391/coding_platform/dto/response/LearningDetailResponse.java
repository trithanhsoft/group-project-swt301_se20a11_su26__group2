package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningDetailResponse {
    Long courseId;
    String courseTitle;
    String instructorName;
    Integer progressPercentage;
    
    // Active Lesson Information
    Integer activeLessonId;
    String activeLessonTitle;
    String activeLessonVideoUrl;
    String activeLessonTheoryContent;
}
