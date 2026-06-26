package com.swp391.coding_platform.controller.judge0;

import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.request.OjSubmissionRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.OjSubmissionInitialResponse;
import com.swp391.coding_platform.service.judge0.Judge0Service;
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
@RequiredArgsConstructor
@RequestMapping("/online-judge")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Judge0Controller {

        Judge0Service judge0Service;

        @PostMapping("/submissions")
        public ResponseEntity<ApiResponse<OjSubmissionInitialResponse>> submitCode(
                        @Valid @RequestBody OjSubmissionRequest request,
                        @AuthenticationPrincipal Jwt jwt) {

                Number claimValue = jwt.getClaim("userId");
                Integer mockUserId = claimValue != null ? claimValue.intValue() : null;
                var result = judge0Service.submitCode(request, mockUserId);

                return ResponseEntity.ok(ApiResponse.<OjSubmissionInitialResponse>builder()
                                .status(200)
                                .code(1000)
                                .message("Submit problem successfully")
                                .result(result)
                                .timestamp(Instant.now().toString())
                                .build());
        }

        @PutMapping("/submissions")
        public ResponseEntity<ApiResponse<Void>> processJudge0Callback(@RequestBody Judge0CallbackPayload payload) {
                log.info("➔ Nhận Webhook từ Judge0 cho token: {}, Trạng thái: {}",
                                payload.getToken(),
                                payload.getStatus() != null ? payload.getStatus().getDescription() : "UNKNOWN");

                judge0Service.processJudge0Callback(payload);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(204)
                                .code(1000)
                                .message("Judge0 callback processed successfully")
                                .result(null)
                                .timestamp(Instant.now().toString())
                                .build());
        }
}