package com.swp391.coding_platform.event;

import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventModelsTest {

    @Test
    void testSubmissionJudgedEvent() {
        Instant now = Instant.now();
        SubmissionJudgedEvent event = SubmissionJudgedEvent.builder()
                .submissionId(1)
                .userId(2)
                .contestId(3)
                .problemId(4)
                .verdict("AC")
                .submitTime(now)
                .build();

        assertEquals(1, event.getSubmissionId());
        assertEquals(2, event.getUserId());
        assertEquals(3, event.getContestId());
        assertEquals(4, event.getProblemId());
        assertEquals("AC", event.getVerdict());
        assertEquals(now, event.getSubmitTime());

        // Test setter
        event.setVerdict("WA");
        assertEquals("WA", event.getVerdict());
    }

    @Test
    void testUserRegisteredEvent() {
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("testuser");

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userEntity(mockUser)
                .build();

        assertNotNull(event.getUserEntity());
        assertEquals("testuser", event.getUserEntity().getUsername());

        // Test setter
        UserEntity mockUser2 = new UserEntity();
        mockUser2.setUsername("user2");
        event.setUserEntity(mockUser2);
        
        assertEquals("user2", event.getUserEntity().getUsername());
    }
}
