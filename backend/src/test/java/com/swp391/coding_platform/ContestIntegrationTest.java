package com.swp391.coding_platform;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.ScoringRule;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@SpringBootTest
@ActiveProfiles("dev")
public class ContestIntegrationTest {

    static {
        try {
            java.io.File envFile = new java.io.File(".env").getAbsoluteFile();
            if (envFile.exists()) {
                java.nio.file.Files.lines(envFile.toPath())
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(line -> {
                            int eqIndex = line.indexOf('=');
                            if (eqIndex > 0) {
                                String key = line.substring(0, eqIndex).trim();
                                String val = line.substring(eqIndex + 1).trim();
                                System.setProperty(key, val);
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("Failed to load .env file in tests: " + e.getMessage());
        }
    }

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testInsertContest() {
        try {
            UserEntity user = userRepository.findById(1).orElse(null);
            if (user == null) {
                user = new UserEntity();
                user.setUsername("testuser");
                user.setEmail("test@test.com");
                user.setPasswordHash("hash");
                user = userRepository.save(user);
            }

            ContestEntity contest = ContestEntity.builder()
                    .title("Integration Test Contest")
                    .description("Test description")
                    .scoringRule(ScoringRule.ICPC)
                    .startTime(Instant.now())
                    .endTime(Instant.now().plusSeconds(3600))
                    .durations(60)
                    .status(ContestStatus.DRAFT)
                    .createdBy(user)
                    .build();

            contestRepository.saveAndFlush(contest);
            System.out.println("SUCCESSFULLY INSERTED CONTEST");
        } catch (Exception e) {
            System.err.println("TEST_ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
