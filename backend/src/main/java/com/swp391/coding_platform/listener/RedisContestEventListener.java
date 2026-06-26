package com.swp391.coding_platform.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.event.SubmissionJudgedEvent;
import com.swp391.coding_platform.service.contest.ContestRankingService;
import com.swp391.coding_platform.service.contest.SseScoreboardManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisContestEventListener implements MessageListener {

    ObjectMapper objectMapper;
    ContestRankingService contestRankingService;
    SseScoreboardManager sseScoreboardManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] body = message.getBody();
            SubmissionJudgedEvent event = objectMapper.readValue(body, SubmissionJudgedEvent.class);
            log.info("Received SubmissionJudgedEvent from Redis Pub/Sub: {}", event);

            if (event.getContestId() != null) {
                // Cập nhật điểm và bảng xếp hạng trên Redis
                Object scoreboardPayload = contestRankingService.updateContestRanking(event);

                // Phát tin thời gian thực qua SSE
                if (scoreboardPayload != null) {
                    sseScoreboardManager.broadcast(event.getContestId(), scoreboardPayload);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process message from Redis channel", e);
        }
    }
}
