package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemAttemptEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ContestProblemAttemptRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContestProblemAttemptRepository repository;

    private UserEntity user;
    private ContestEntity contest;
    private ProblemEntity problem;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("attempt_testuser")
                .displayname("Test User")
                .email("attempt_testuser@example.com")
                .build();
        user = entityManager.persistAndFlush(user);

        contest = ContestEntity.builder()
                .createdBy(user)
                .title("Attempt Test Contest")
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .durations(7200)
                .build();
        contest = entityManager.persistAndFlush(contest);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .build();
        problem = entityManager.persistAndFlush(problem);
    }

    @Test
    void testFindByContestIdAndUserId() {
        ContestProblemAttemptEntity attempt1 = ContestProblemAttemptEntity.builder()
                .contest(contest)
                .user(user)
                .problem(problem)
                .isSolved(true)
                .build();
        
        ProblemEntity problem2 = ProblemEntity.builder().createdBy(user).build();
        problem2 = entityManager.persistAndFlush(problem2);
        
        ContestProblemAttemptEntity attempt2 = ContestProblemAttemptEntity.builder()
                .contest(contest)
                .user(user)
                .problem(problem2)
                .isSolved(false)
                .build();

        entityManager.persist(attempt1);
        entityManager.persist(attempt2);
        entityManager.flush();

        List<ContestProblemAttemptEntity> attempts = repository.findByContestIdAndUserId(contest.getId(), user.getId());
        assertEquals(2, attempts.size());
    }

    @Test
    void testFindByContestIdAndUserIdAndProblemId() {
        ContestProblemAttemptEntity attempt = ContestProblemAttemptEntity.builder()
                .contest(contest)
                .user(user)
                .problem(problem)
                .isSolved(true)
                .build();
        entityManager.persistAndFlush(attempt);

        Optional<ContestProblemAttemptEntity> result = repository.findByContestIdAndUserIdAndProblemId(contest.getId(), user.getId(), problem.getId());
        assertTrue(result.isPresent());
        assertEquals(attempt.getId(), result.get().getId());
        assertEquals(contest.getId(), result.get().getContest().getId());
        assertEquals(user.getId(), result.get().getUser().getId());
        assertEquals(problem.getId(), result.get().getProblem().getId());
        
        Optional<ContestProblemAttemptEntity> notFoundResult = repository.findByContestIdAndUserIdAndProblemId(contest.getId(), user.getId(), problem.getId() + 999);
        assertFalse(notFoundResult.isPresent());
    }
}
