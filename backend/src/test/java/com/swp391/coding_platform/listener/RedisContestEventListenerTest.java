package com.swp391.coding_platform.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.event.SubmissionJudgedEvent;
import com.swp391.coding_platform.service.contest.ContestRankingService;
import com.swp391.coding_platform.service.contest.SseScoreboardManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisContestEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ContestRankingService contestRankingService;

    @Mock
    private SseScoreboardManager sseScoreboardManager;

    @InjectMocks
    private RedisContestEventListener listener;

    @Test
    void onMessage_WithValidContestIdAndScoreboardPayload_ShouldBroadcast() throws Exception {
        Message message = mock(Message.class);
        byte[] body = "{}".getBytes();
        when(message.getBody()).thenReturn(body);

        SubmissionJudgedEvent event = new SubmissionJudgedEvent();
        event.setContestId(10);
        when(objectMapper.readValue(body, SubmissionJudgedEvent.class)).thenReturn(event);

        com.swp391.coding_platform.dto.response.ContestScoreboardResponse scoreboardPayload = new com.swp391.coding_platform.dto.response.ContestScoreboardResponse();
        when(contestRankingService.updateContestRanking(event)).thenReturn(scoreboardPayload);

        listener.onMessage(message, null);

        verify(contestRankingService).updateContestRanking(event);
        verify(sseScoreboardManager).broadcast(10, scoreboardPayload);
    }

    @Test
    void onMessage_WithNullContestId_ShouldNotUpdateRanking() throws Exception {
        Message message = mock(Message.class);
        byte[] body = "{}".getBytes();
        when(message.getBody()).thenReturn(body);

        SubmissionJudgedEvent event = new SubmissionJudgedEvent();
        event.setContestId(null);
        when(objectMapper.readValue(body, SubmissionJudgedEvent.class)).thenReturn(event);

        listener.onMessage(message, null);

        verify(contestRankingService, never()).updateContestRanking(any());
        verify(sseScoreboardManager, never()).broadcast(any(), any());
    }

    @Test
    void onMessage_WithNullScoreboardPayload_ShouldNotBroadcast() throws Exception {
        Message message = mock(Message.class);
        byte[] body = "{}".getBytes();
        when(message.getBody()).thenReturn(body);

        SubmissionJudgedEvent event = new SubmissionJudgedEvent();
        event.setContestId(10);
        when(objectMapper.readValue(body, SubmissionJudgedEvent.class)).thenReturn(event);

        when(contestRankingService.updateContestRanking(event)).thenReturn(null);

        listener.onMessage(message, null);

        verify(contestRankingService).updateContestRanking(event);
        verify(sseScoreboardManager, never()).broadcast(any(), any());
    }

    @Test
    void onMessage_WhenObjectMapperThrowsException_ShouldCatchException() throws Exception {
        Message message = mock(Message.class);
        byte[] body = "invalid".getBytes();
        when(message.getBody()).thenReturn(body);

        when(objectMapper.readValue(body, SubmissionJudgedEvent.class)).thenThrow(new RuntimeException("Parsing error"));

        // Phương thức không được phép ném lỗi ra ngoài làm gián đoạn Redis
        listener.onMessage(message, null);

        verify(contestRankingService, never()).updateContestRanking(any());
        verify(sseScoreboardManager, never()).broadcast(any(), any());
    }
}
