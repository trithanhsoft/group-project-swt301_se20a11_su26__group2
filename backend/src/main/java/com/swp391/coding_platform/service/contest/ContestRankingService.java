package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.response.ContestScoreboardResponse;
import com.swp391.coding_platform.dto.response.ContestScoreboardResponse.ProblemSummary;
import com.swp391.coding_platform.dto.response.ContestScoreboardResponse.TeamRow;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.event.SubmissionJudgedEvent;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.configuration.RabbitMQConfig;
import com.swp391.coding_platform.dto.message.ContestRankingDbUpdateMessage;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestRankingService {

    StringRedisTemplate stringRedisTemplate;
    ContestRepository contestRepository;
    ContestProblemRepository contestProblemRepository;
    UserRepository userRepository;
    RabbitTemplate rabbitTemplate;

    @Transactional
    public ContestScoreboardResponse updateContestRanking(SubmissionJudgedEvent event) {
        Integer contestId = event.getContestId();
        Integer userId = event.getUserId();
        Integer problemId = event.getProblemId();
        String verdict = event.getVerdict();
        Instant submitTime = event.getSubmitTime();

        ContestEntity contest = contestRepository.findById(contestId).orElse(null);
        if (contest == null) {
            log.warn("Contest not found for id: {}", contestId);
            return null;
        }

        // Redis Key cho participant state (live vs public)
        String liveHashKey = "contest:participant:" + contestId + ":" + userId + ":live";
        String publicHashKey = "contest:participant:" + contestId + ":" + userId + ":public";
        String field = "problem_" + problemId;

        // format: is_ac:wrong_attempts_before_ac:first_ac_time_seconds
        String currentStatus = (String) stringRedisTemplate.opsForHash().get(liveHashKey, field);
        
        int isAc = 0;
        int wrongAttempts = 0;
        long firstAcTimeSeconds = 0;

        if (currentStatus != null && !currentStatus.trim().isEmpty()) {
            String[] parts = currentStatus.split(":");
            if (parts.length >= 3) {
                isAc = Integer.parseInt(parts[0]);
                wrongAttempts = Integer.parseInt(parts[1]);
                firstAcTimeSeconds = Long.parseLong(parts[2]);
            }
        }

        // Nếu đã AC bài này trước đó, bỏ qua toàn bộ logic
        if (isAc == 1) {
            log.info("User {} already solved problem {} in contest {}. Ignoring current submission.", userId, problemId, contestId);
            return getScoreboard(contestId, false); // Trả về public scoreboard để stream
        }

        boolean verdictIsAc = "ACCEPTED".equalsIgnoreCase(verdict);
        
        if (verdictIsAc) {
            isAc = 1;
            // Tính toán khoảng thời gian từ lúc bắt đầu contest đến lúc submit
            long diffSeconds = Duration.between(contest.getStartTime(), submitTime).getSeconds();
            if (diffSeconds < 0) {
                diffSeconds = 0;
            }
            firstAcTimeSeconds = diffSeconds;
            
            // Lưu người đầu tiên giải được (first solver) bài này
            String firstSolveKey = "contest:first_solve:" + contestId + ":" + problemId;
            stringRedisTemplate.opsForValue().setIfAbsent(firstSolveKey, String.valueOf(userId));
        } else if (!"COMPILATION_ERROR".equalsIgnoreCase(verdict)) {
            // Lỗi compile thường không tính là attempt bị phạt
            wrongAttempts += 1;
        }

        // Lưu trạng thái mới vào Redis Hash (Live)
        String newStatus = isAc + ":" + wrongAttempts + ":" + firstAcTimeSeconds;
        stringRedisTemplate.opsForHash().put(liveHashKey, field, newStatus);
        log.info("Updated live status for user {} on problem {} to {}", userId, problemId, newStatus);

        // Duyệt qua tất cả các bài toán của user đó trong Contest để tính điểm tổng hợp (dựa trên Live Hash)
        List<ContestProblemEntity> contestProblems = contestProblemRepository.findByContestIdWithProblem(contestId);
        
        int problemsSolved = 0;
        long totalPenaltySeconds = 0;

        for (ContestProblemEntity cp : contestProblems) {
            String f = "problem_" + cp.getProblem().getId();
            String status = (String) stringRedisTemplate.opsForHash().get(liveHashKey, f);
            if (status != null && !status.trim().isEmpty()) {
                String[] parts = status.split(":");
                if (parts.length >= 3) {
                    int pAc = Integer.parseInt(parts[0]);
                    int pWrong = Integer.parseInt(parts[1]);
                    long pAcTime = Long.parseLong(parts[2]);

                    if (pAc == 1) {
                        problemsSolved += 1;
                        totalPenaltySeconds += pAcTime + ((long) pWrong * 20 * 60);
                    }
                }
            }
        }

        // Tính toán Score nguyên bản dịch chuyển (ZSET Score)
        // Formula: score = (problemsSolved * 10^10) + (10^9 - totalPenaltySeconds)
        long baseScore = 1_000_000_000L;
        double zsetScore = ((double) problemsSolved * 1e10) + (baseScore - totalPenaltySeconds);

        // Cập nhật ZSET Live Scoreboard
        String liveScoreboardKey = "contest:scoreboard:" + contestId + ":live";
        stringRedisTemplate.opsForZSet().add(liveScoreboardKey, String.valueOf(userId), zsetScore);

        // Xử lý Contest Freeze (Đóng băng bảng điểm khi còn 15% thời gian cuối)
        Instant endTime = contest.getEndTime();
        Instant startTime = contest.getStartTime();
        long totalDurationSeconds = Duration.between(startTime, endTime).getSeconds();
        long freezeDurationSeconds = totalDurationSeconds * 15 / 100;
        Instant freezeTime = endTime.minus(Duration.ofSeconds(freezeDurationSeconds));

        String publicScoreboardKey = "contest:scoreboard:" + contestId + ":public";
        if (submitTime.isBefore(freezeTime)) {
            // Nếu nộp trước thời gian đóng băng, cập nhật ZSET Public Scoreboard và cả Public Hash
            stringRedisTemplate.opsForZSet().add(publicScoreboardKey, String.valueOf(userId), zsetScore);
            stringRedisTemplate.opsForHash().put(publicHashKey, field, newStatus);
            log.info("Updated public status and scoreboard for user {} on problem {} (before freeze)", userId, problemId);
        } else {
            // Nếu nộp trong thời gian đóng băng, KHÔNG cập nhật public scoreboard/hash mới, nhưng ZADD người mới vào nếu chưa có
            // để họ hiển thị ở trạng thái ban đầu của lúc trước freeze
            Double existingPublicScore = stringRedisTemplate.opsForZSet().score(publicScoreboardKey, String.valueOf(userId));
            if (existingPublicScore == null) {
                double defaultScore = (double) baseScore;
                stringRedisTemplate.opsForZSet().add(publicScoreboardKey, String.valueOf(userId), defaultScore);
            }
        }

        // ==========================================
        // PERSIST RANKING TO DATABASE (Lưu xuống DB để không mất khi Redis restart)
        // ==========================================
        ContestRankingDbUpdateMessage dbMessage = ContestRankingDbUpdateMessage.builder()
                .contestId(contestId)
                .userId(userId)
                .problemsSolved(problemsSolved)
                .totalPenaltyMinutes((int) (totalPenaltySeconds / 60))
                .problemId(problemId)
                .isSolved(isAc == 1)
                .solvedAtSeconds(isAc == 1 ? (int) firstAcTimeSeconds : null)
                .failedAttemptsCount(wrongAttempts)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTEST_RANKING_DB_UPDATE_QUEUE, dbMessage);
        log.info("Sent contest ranking DB update message to RabbitMQ for user {} in contest {}", userId, contestId);

        // Trả về scoreboard để stream (mặc định stream bảng public cho đại chúng)
        return getScoreboard(contestId, false);
    }

    public ContestScoreboardResponse getScoreboard(Integer contestId, boolean isLive) {
        // Tự động giải băng nếu contest đã kết thúc
        boolean finalLive = isLive;
        if (!finalLive) {
            ContestEntity contest = contestRepository.findById(contestId).orElse(null);
            if (contest != null && Instant.now().isAfter(contest.getEndTime())) {
                finalLive = true;
            }
        }

        String zsetKey = "contest:scoreboard:" + contestId + (finalLive ? ":live" : ":public");

        Set<ZSetOperations.TypedTuple<String>> rankedMembers = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(zsetKey, 0, -1);

        if (rankedMembers == null || rankedMembers.isEmpty()) {
            return ContestScoreboardResponse.builder()
                    .contestId(contestId)
                    .rows(Collections.emptyList())
                    .build();
        }

        List<ContestProblemEntity> contestProblems = contestProblemRepository.findByContestIdWithProblem(contestId);
        contestProblems.sort(Comparator.comparing(ContestProblemEntity::getOrderIndex));

        List<Integer> userIds = rankedMembers.stream()
                .map(m -> Integer.parseInt(m.getValue()))
                .collect(Collectors.toList());

        List<UserEntity> users = userRepository.findAllById(userIds);
        Map<Integer, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<TeamRow> rows = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : rankedMembers) {
            Integer userId = Integer.parseInt(tuple.getValue());
            Double scoreVal = tuple.getScore();
            if (scoreVal == null) continue;

            long scoreLong = scoreVal.longValue();
            int solved = (int) (scoreLong / 10_000_000_000L);
            long penaltySeconds = 1_000_000_000L - (scoreLong % 10_000_000_000L);
            int penaltyMinutes = (int) (penaltySeconds / 60);

            UserEntity user = userMap.get(userId);
            String name = user != null ? user.getUsername() : "User_" + userId;
            String displayName = user != null ? user.getDisplayname() : "User_" + userId;
            String affiliation = "Participant";

            Map<String, ProblemSummary> submissionsMap = new HashMap<>();
            int totalAttempts = 0;

            String participantHashKey = "contest:participant:" + contestId + ":" + userId + (finalLive ? ":live" : ":public");

            for (ContestProblemEntity cp : contestProblems) {
                String label = String.valueOf((char) ('A' + cp.getOrderIndex()));
                String f = "problem_" + cp.getProblem().getId();
                String statusStr = (String) stringRedisTemplate.opsForHash().get(participantHashKey, f);

                int isAc = 0;
                int wrongAttempts = 0;
                long acTimeSec = 0;

                if (statusStr != null && !statusStr.trim().isEmpty()) {
                    String[] parts = statusStr.split(":");
                    if (parts.length >= 3) {
                        isAc = Integer.parseInt(parts[0]);
                        wrongAttempts = Integer.parseInt(parts[1]);
                        acTimeSec = Long.parseLong(parts[2]);
                    }
                }

                totalAttempts += wrongAttempts + isAc;

                String status = "unattempted";
                if (isAc == 1) {
                    String firstSolveKey = "contest:first_solve:" + contestId + ":" + cp.getProblem().getId();
                    String firstSolverId = stringRedisTemplate.opsForValue().get(firstSolveKey);
                    if (String.valueOf(userId).equals(firstSolverId)) {
                        status = "first_solve";
                    } else {
                        status = "accepted";
                    }
                } else if (wrongAttempts > 0) {
                    status = "failed";
                }

                String timeStr = isAc == 1 ? formatElapsed(acTimeSec * 1000) : null;

                submissionsMap.put(label, ProblemSummary.builder()
                        .time(timeStr)
                        .penalty(wrongAttempts)
                        .status(status)
                        .build());
            }

            rows.add(TeamRow.builder()
                    .rank(rank++)
                    .userId(userId)
                    .name(name)
                    .displayName(displayName)
                    .affiliation(affiliation)
                    .solved(solved)
                    .totalAttempts(totalAttempts)
                    .totalPenalty(penaltyMinutes)
                    .submissions(submissionsMap)
                    .build());
        }

        return ContestScoreboardResponse.builder()
                .contestId(contestId)
                .rows(rows)
                .build();
    }

    private String formatElapsed(long elapsedMs) {
        long totalSecs = elapsedMs / 1000;
        long hrs = totalSecs / 3600;
        long mins = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;
        return String.format("%d:%02d:%02d", hrs, mins, secs);
    }
}
