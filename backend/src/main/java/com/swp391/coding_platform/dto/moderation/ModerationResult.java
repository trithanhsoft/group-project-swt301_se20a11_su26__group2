package com.swp391.coding_platform.dto.moderation;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResult {
    private Boolean isClean;
    private List<String> courseViolations;
    private List<LessonViolation> lessonViolations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonViolation {
        private Long lessonId;
        private String lessonTitle;
        private String violationType;
        private String reason;
    }
}
