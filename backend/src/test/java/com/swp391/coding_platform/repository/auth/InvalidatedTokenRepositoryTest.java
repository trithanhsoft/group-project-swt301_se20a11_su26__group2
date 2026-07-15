package com.swp391.coding_platform.repository.auth;

import com.swp391.coding_platform.entity.auth.InvalidatedTokenEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class InvalidatedTokenRepositoryTest {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Test
    void existsByTokenJti_True() {
        InvalidatedTokenEntity token = InvalidatedTokenEntity.builder()
                .tokenJti("token123")
                .expiryTime(OffsetDateTime.now().plusDays(1))
                .build();
        invalidatedTokenRepository.save(token);

        boolean exists = invalidatedTokenRepository.existsByTokenJti("token123");
        assertTrue(exists);
    }

    @Test
    void existsByTokenJti_False() {
        boolean exists = invalidatedTokenRepository.existsByTokenJti("nonexistent");
        assertFalse(exists);
    }
}

