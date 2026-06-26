package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "OLD_PASSWORD_INVALID")
    @Size(min = 4, message = "OLD_PASSWORD_INVALID")
    String oldPassword;

    @NotBlank(message = "NEW_PASSWORD_INVALID")
    @Size(min = 4, message = "NEW_PASSWORD_INVALID")
    String newPassword;

    @NotBlank(message = "CONFIRM_NEW_PASSWORD_INVALID")
    @Size(min = 4, message = "CONFIRM_NEW_PASSWORD_INVALID")
    String confirmNewPassword;
}
