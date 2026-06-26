package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructorCourseDetailResponse {
    Long id;
    String title;
    String thumbnailUrl;
    String shortDescription;
    String longDescription;
    String whatYouLearn;
    String courseHighlight;
    String technologyTool;
    String prerequisites;
    String targetAudience;
    String completionBenefits;
    BigDecimal price;
    String topic;
    String status;
    List<InstructorChapterResponse> chapters;
    List<CategoryDto> categories;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CategoryDto {
        Integer id;
        String name;
    }
}
