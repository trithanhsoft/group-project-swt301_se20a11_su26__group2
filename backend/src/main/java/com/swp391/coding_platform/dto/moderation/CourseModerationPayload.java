package com.swp391.coding_platform.dto.moderation;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModerationPayload {
    private String courseTitle;
    private String description;
    private List<ChapterPayload> chapters;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterPayload {
        private String chapterTitle;
        private List<LessonPayload> lessons;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonPayload {
        private Long lessonId;
        private String lessonTitle;
        private String theoryText;
        private String videoTranscript;
        private List<QuizPayload> quizzes;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizPayload {
        private String title;
        private String question;
        private List<String> options;
    }
}
