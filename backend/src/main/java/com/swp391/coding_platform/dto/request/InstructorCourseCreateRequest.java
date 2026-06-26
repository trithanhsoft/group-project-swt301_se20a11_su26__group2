package com.swp391.coding_platform.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructorCourseCreateRequest {
    String title;
    String shortDescription;
    String longDescription;
    java.util.List<Integer> categoryIds;
    Boolean isFree;
    BigDecimal price;
    java.util.List<String> whatYouLearn;
    java.util.List<String> courseHighlight;
    java.util.List<String> technologyTool;
    java.util.List<String> prerequisites;
    java.util.List<String> targetAudience;
    java.util.List<String> completionBenefits;
    String thumbnailUrl;
}
