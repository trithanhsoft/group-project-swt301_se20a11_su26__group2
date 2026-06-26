package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveApplicationRequest {

    @NotBlank(message = "Status cannot be blank")
    String status; // APPROVED or REJECTED

    String adminNote;
}
