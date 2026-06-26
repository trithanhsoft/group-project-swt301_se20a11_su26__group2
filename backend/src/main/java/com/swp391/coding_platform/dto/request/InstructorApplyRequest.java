package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstructorApplyRequest {

    @NotBlank(message = "Full name cannot be blank")
    String fullName;

    @NotBlank(message = "Major cannot be blank")
    String major;

    @NotBlank(message = "Bio cannot be blank")
    String bio;
}
