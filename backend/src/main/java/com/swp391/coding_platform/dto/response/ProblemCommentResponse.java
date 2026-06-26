package com.swp391.coding_platform.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCommentResponse {
    Integer id;
    String author;
    @JsonProperty("avatar_url")
    String avatarUrl;
    String text;
    String time;
    Instant createdAt;
    Integer parentId;
    List<ProblemCommentResponse> replies;
}
