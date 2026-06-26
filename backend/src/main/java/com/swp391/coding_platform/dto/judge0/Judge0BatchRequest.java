package com.swp391.coding_platform.dto.judge0;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Judge0BatchRequest {
    List<Judge0SubmissionItem> submissions;
}


