package com.swp391.coding_platform.controller.ranking;

import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.RankingUserResponse;
import com.swp391.coding_platform.dto.response.UserRankStatsResponse;
import com.swp391.coding_platform.service.ranking.RankingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RankingController {

    RankingService rankingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RankingUserResponse>>> getGlobalRankings(
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        
        List<RankingUserResponse> result = rankingService.getGlobalRankings(filter);

        return ResponseEntity.ok(ApiResponse.<List<RankingUserResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get global rankings successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserRankStatsResponse>> getUserRankStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        
        Integer userId = null;
        if (jwt != null) {
            Number userIdNum = jwt.getClaim("userId");
            if (userIdNum != null) {
                userId = userIdNum.intValue();
            }
        }

        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.<UserRankStatsResponse>builder()
                    .status(401)
                    .code(1005)
                    .message("Unauthenticated")
                    .timestamp(Instant.now().toString())
                    .build());
        }

        UserRankStatsResponse result = rankingService.getUserRankStats(userId, filter);

        return ResponseEntity.ok(ApiResponse.<UserRankStatsResponse>builder()
                .status(200)
                .code(1000)
                .message("Get user rank stats successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }
}
