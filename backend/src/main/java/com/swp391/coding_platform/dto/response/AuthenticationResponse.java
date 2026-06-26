package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    Long id;
    String displayName;
    String avatarUrl;
    String email;
    BigDecimal balance;
    Set<String> roles;
    String username;
    String status;
    String lockReason;
    String lockAppeal;
}
