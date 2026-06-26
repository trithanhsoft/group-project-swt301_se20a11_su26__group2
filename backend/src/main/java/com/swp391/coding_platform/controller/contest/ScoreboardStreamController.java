package com.swp391.coding_platform.controller.contest;

import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.ContestScoreboardResponse;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.service.contest.ContestRankingService;
import com.swp391.coding_platform.service.contest.SseScoreboardManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contests/{contestId}/scoreboard")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScoreboardStreamController {

    ContestRankingService contestRankingService;
    SseScoreboardManager sseScoreboardManager;
    ContestRepository contestRepository;

    /**
     * GET /scoreboard — Snapshot tĩnh của bảng xếp hạng (JSON).
     *
     * Truy cập được trong cả ONGOING lẫn ENDED (để review kết quả sau khi thi).
     * Chỉ block khi UPCOMING.
     *
     * - Admin: có thể thêm ?live=true để xem bảng điểm thực (không freeze)
     * - User đã đăng ký: luôn thấy bảng điểm public (có freeze trong contest)
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ContestScoreboardResponse>> getScoreboard(
            @PathVariable Integer contestId,
            @RequestParam(value = "live", defaultValue = "false") boolean isLive,
            JwtAuthenticationToken token) {

        boolean isAdmin = token.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (!isAdmin) {
            Integer userId = Integer.parseInt(token.getToken().getClaim("userId").toString());
            boolean isRegistered = contestRepository.isUserRegistered(contestId, userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }
            // Block chỉ khi UPCOMING — ENDED vẫn cho xem để review kết quả
            Instant now = Instant.now();
            if (now.isBefore(contest.getStartTime())) {
                throw new AppException(ErrorCode.CONTEST_NOT_STARTED);
            }
        }

        // User chỉ được xem live scoreboard nếu là admin
        boolean requestedLive = isLive && isAdmin;

        ContestScoreboardResponse scoreboard = contestRankingService.getScoreboard(contestId, requestedLive);

        return ResponseEntity.ok(ApiResponse.<ContestScoreboardResponse>builder()
                .status(200)
                .code(1000)
                .message("Get contest scoreboard successfully")
                .result(scoreboard)
                .timestamp(Instant.now().toString())
                .build());
    }

    /**
     * GET /scoreboard/stream — SSE live feed của bảng xếp hạng.
     *
     * Chỉ có ý nghĩa trong ONGOING: server push update mỗi khi có submission mới.
     * Block khi UPCOMING (chưa bắt đầu) và ENDED (không còn submission mới — dùng snapshot thay thế).
     *
     * - Admin: xem live stream không bị ảnh hưởng bởi freeze
     * - User đã đăng ký + ONGOING: xem stream public (freeze apply)
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getScoreboardStream(
            @PathVariable Integer contestId,
            JwtAuthenticationToken token) {

        boolean isAdmin = token.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        Instant now = Instant.now();

        if (!isAdmin) {
            Integer userId = Integer.parseInt(token.getToken().getClaim("userId").toString());
            boolean isRegistered = contestRepository.isUserRegistered(contestId, userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }
            // Block khi chưa bắt đầu
            if (now.isBefore(contest.getStartTime())) {
                throw new AppException(ErrorCode.CONTEST_NOT_STARTED);
            }
        }

        // Block SSE khi contest đã kết thúc — dùng GET /scoreboard (snapshot) thay thế
        if (now.isAfter(contest.getEndTime())) {
            throw new AppException(ErrorCode.CONTEST_ALREADY_ENDED);
        }

        log.info("Client subscribed to SSE scoreboard stream for contest: {}", contestId);
        return sseScoreboardManager.createConnection(contestId);
    }
}
