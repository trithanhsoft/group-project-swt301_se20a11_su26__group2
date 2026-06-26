package com.swp391.coding_platform.controller.user;

import com.swp391.coding_platform.dto.request.AppealRequest;
import com.swp391.coding_platform.dto.request.ChangePasswordRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.UserResponse;
import com.swp391.coding_platform.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        String username = jwt.getSubject();
        userService.changePassword(username, changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Password changed successfully")
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal Jwt jwt){
        String username = jwt.getSubject();
        UserResponse userResponse = userService.getMyInfo(username);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(200)
                .code(1000)
                .message("User info retrieved successfully")
                .result(userResponse)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/appeal")
    public ResponseEntity<ApiResponse<Void>> submitAppeal(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid AppealRequest appealRequest) {
        String username = jwt.getSubject();
        userService.submitAppeal(username, appealRequest);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Appeal submitted successfully")
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }
}
