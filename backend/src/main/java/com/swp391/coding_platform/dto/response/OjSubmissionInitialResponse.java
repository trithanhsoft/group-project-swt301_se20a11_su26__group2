package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OjSubmissionInitialResponse {
    Integer submissionId; // Trả về ID của bản ghi tổng vừa tạo trong DB
    String status; // Mặc định trả về "PENDING"
    String message; // "Code is being judged..."
}