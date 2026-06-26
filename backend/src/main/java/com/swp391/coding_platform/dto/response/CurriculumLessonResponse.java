package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurriculumLessonResponse {
    Integer id;
    String title;
    Boolean isTrial;
    Integer orderIndex;
    String videoUrl;
    String type; // 'video' | 'coding' | 'reading'
    String status;
}
