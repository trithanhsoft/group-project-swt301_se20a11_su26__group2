package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
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
public class ContestProblemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContestProblemRepository repository;

    private UserEntity user;
    private ContestEntity contest;
    private ProblemEntity problem;
    private ProblemVersionEntity problemVersion;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("cp_testuser")
                .displayname("Test User")
                .email("cp_testuser@example.com")
                .build();
        user = entityManager.persistAndFlush(user);

        contest = ContestEntity.builder()
                .createdBy(user)
                .title("Test Contest")
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .durations(7200)
                .build();
        contest = entityManager.persistAndFlush(contest);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .build();
        problem = entityManager.persistAndFlush(problem);

        problemVersion = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .problemScope(com.swp391.coding_platform.entity.enums.ProblemScope.PRACTICE)
                .title("Problem Title")
                .description("Problem Description")
                .memoryLimitKb(256000)
                .timeLimitMs(1000)
                .isActive(true)
                .build();
        problemVersion = entityManager.persistAndFlush(problemVersion);
    }

    @Test
    void testExistsByContestIdAndProblemId() {
        ContestProblemEntity cp = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problemVersion)
                .orderIndex(1)
                .build();
        entityManager.persistAndFlush(cp);

        assertTrue(repository.existsByContestIdAndProblemId(contest.getId(), problem.getId()));
        assertFalse(repository.existsByContestIdAndProblemId(contest.getId(), problem.getId() + 999));
    }

    @Test
    void testFindByContestIdAndProblemId() {
        ContestProblemEntity cp = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problemVersion)
                .orderIndex(1)
                .build();
        entityManager.persistAndFlush(cp);

        Optional<ContestProblemEntity> found = repository.findByContestIdAndProblemId(contest.getId(), problem.getId());
        assertTrue(found.isPresent());
        assertEquals(contest.getId(), found.get().getContest().getId());
        assertEquals(problem.getId(), found.get().getProblem().getId());
    }

    @Test
    void testFindByContestIdWithProblem() {
        ProblemEntity problem2 = ProblemEntity.builder().createdBy(user).build();
        problem2 = entityManager.persistAndFlush(problem2);
        
        ProblemVersionEntity problemVersion2 = ProblemVersionEntity.builder()
                .problem(problem2)
                .versionNumber(1)
                .problemScope(com.swp391.coding_platform.entity.enums.ProblemScope.PRACTICE)
                .title("Problem 2 Title")
                .description("Problem 2 Description")
                .memoryLimitKb(256000)
                .timeLimitMs(1000)
                .isActive(true)
                .build();
        problemVersion2 = entityManager.persistAndFlush(problemVersion2);

        ContestProblemEntity cp1 = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problemVersion)
                .orderIndex(2)
                .build();
        
        ContestProblemEntity cp2 = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem2)
                .problemVersion(problemVersion2)
                .orderIndex(1)
                .build();

        entityManager.persist(cp1);
        entityManager.persist(cp2);
        entityManager.flush();
        entityManager.clear();

        List<ContestProblemEntity> list = repository.findByContestIdWithProblem(contest.getId());
        assertEquals(2, list.size());
        // Check order
        assertEquals(problem2.getId(), list.get(0).getProblem().getId());
        assertEquals(problem.getId(), list.get(1).getProblem().getId());
    }

    @Test
    void testDeleteByContestId() {
        ContestProblemEntity cp = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problemVersion)
                .orderIndex(1)
                .build();
        entityManager.persistAndFlush(cp);

        repository.deleteByContestId(contest.getId());
        entityManager.flush();

        List<ContestProblemEntity> list = repository.findAll();
        assertTrue(list.isEmpty());
    }
}
