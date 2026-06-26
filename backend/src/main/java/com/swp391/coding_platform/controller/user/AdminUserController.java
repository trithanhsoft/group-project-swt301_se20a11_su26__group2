package com.swp391.coding_platform.controller.user;

import com.swp391.coding_platform.dto.request.LockUserRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.AdminUserResponse;
import com.swp391.coding_platform.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers() {
        List<AdminUserResponse> result = userService.getAllUsersForAdmin();

        return ResponseEntity.ok(ApiResponse.<List<AdminUserResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched all users successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{userId}/lock")
    public ResponseEntity<ApiResponse<AdminUserResponse>> lockOrUnlockUser(
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody LockUserRequest request) {

        AdminUserResponse result = userService.setUserLockStatus(userId, request);

        return ResponseEntity.ok(ApiResponse.<AdminUserResponse>builder()
                .status(200)
                .code(1000)
                .message("User status updated successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
