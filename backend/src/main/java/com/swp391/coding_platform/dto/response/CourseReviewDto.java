package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseReviewDto {
    Integer id;
    String content;
    Integer star;
    String displayName;
    String avatarUrl;
    Instant createdAt;
}
