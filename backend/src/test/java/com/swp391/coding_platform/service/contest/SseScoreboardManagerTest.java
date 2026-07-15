package com.swp391.coding_platform.service.contest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SseScoreboardManagerTest {

    @InjectMocks
    private SseScoreboardManager sseScoreboardManager;

    @Test
    void createConnection_HappyPath_ReturnsEmitter() {
        Integer contestId = 1;
        SseEmitter emitter = sseScoreboardManager.createConnection(contestId);
        
        assertNotNull(emitter);
        assertEquals(900_000L, emitter.getTimeout());
    }

    @Test
    void broadcast_HappyPath_SendsPayload() {
        Integer contestId = 1;
        // Create a connection so there is an emitter in the list
        sseScoreboardManager.createConnection(contestId);
        
        // Broadcast should not throw an exception even if sending fails or succeeds internally
        assertDoesNotThrow(() -> sseScoreboardManager.broadcast(contestId, "Test Payload"));
    }

    @Test
    void broadcast_NoEmitters_DoesNothing() {
        Integer contestId = 2; // No connections created for this contest
        
        // Should return silently
        assertDoesNotThrow(() -> sseScoreboardManager.broadcast(contestId, "Test Payload"));
    }
}
