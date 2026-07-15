package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.*;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.projection.SubmissionMaxStats;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ProblemSubmissionDetailRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemSubmissionDetailRepository repository;

    private UserEntity user;
    private ProblemEntity problem;
    private ProblemVersionEntity problemVersion;
    private ProblemSubmissionEntity submission;
    private ProblemTestcaseEntity testcase1;
    private ProblemTestcaseEntity testcase2;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("detail_testuser")
                .displayname("Test User")
                .email("detail_testuser@example.com")
                .build();
        user = entityManager.persistAndFlush(user);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .build();
        problem = entityManager.persistAndFlush(problem);

        problemVersion = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .title("Problem Title")
                .description("Problem Description")
                .problemScope(ProblemScope.PRACTICE)
                .timeLimitMs(1000)
                .memoryLimitKb(128000)
                .isActive(true)
                .build();
        problemVersion = entityManager.persistAndFlush(problemVersion);

        testcase1 = ProblemTestcaseEntity.builder()
                .problemVersion(problemVersion)
                .inputData("1 2")
                .expectedOutput("3")
                .orderIndex(1)
                .build();
        testcase1 = entityManager.persistAndFlush(testcase1);

        testcase2 = ProblemTestcaseEntity.builder()
                .problemVersion(problemVersion)
                .inputData("2 3")
                .expectedOutput("5")
                .orderIndex(2)
                .build();
        testcase2 = entityManager.persistAndFlush(testcase2);

        submission = ProblemSubmissionEntity.builder()
                .problem(problem)
                .problemVersion(problemVersion)
                .user(user)
                .languageId(50) // e.g. C++
                .sourceCode("test code")
                .build();
        submission = entityManager.persistAndFlush(submission);
    }

    @Test
    void testFindBySubmissionId() {
        ProblemSubmissionDetailEntity detail = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase1)
                .build();
        entityManager.persistAndFlush(detail);

        List<ProblemSubmissionDetailEntity> details = repository.findBySubmissionId(submission.getId());
        assertEquals(1, details.size());
        assertEquals(submission.getId(), details.get(0).getSubmission().getId());
    }

    @Test
    void testFindByTokenWithSubmissionAndProblem() {
        ProblemSubmissionDetailEntity detail = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase1)
                .token("test-token-123")
                .build();
        entityManager.persistAndFlush(detail);

        entityManager.clear(); // Ensure it comes from DB with fetched associations

        Optional<ProblemSubmissionDetailEntity> result = repository.findByTokenWithSubmissionAndProblem("test-token-123");
        assertTrue(result.isPresent());
        assertEquals("test-token-123", result.get().getToken());
        assertNotNull(result.get().getSubmission());
        assertNotNull(result.get().getSubmission().getProblem());
    }

    @Test
    void testFindFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc() {
        ProblemSubmissionDetailEntity detail1 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase2)
                .verdict(OjVerdict.ACCEPTED)
                .build();
        entityManager.persist(detail1);

        ProblemSubmissionDetailEntity detail2 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase1)
                .verdict(OjVerdict.WRONG_ANSWER)
                .build();
        entityManager.persist(detail2);
        
        entityManager.flush();

        Optional<ProblemSubmissionDetailEntity> result = repository.findFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc(
                submission.getId(), OjVerdict.ACCEPTED);
        
        assertTrue(result.isPresent());
        assertEquals(OjVerdict.WRONG_ANSWER, result.get().getVerdict());
        assertEquals(testcase1.getId(), result.get().getTestcase().getId());
    }

    @Test
    void testFindMaxStatsBySubmissionId() {
        ProblemSubmissionDetailEntity detail1 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase1)
                .executionTime(10)
                .memoryUsed(100)
                .build();
        entityManager.persist(detail1);

        ProblemSubmissionDetailEntity detail2 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase2)
                .executionTime(25)
                .memoryUsed(50)
                .build();
        entityManager.persist(detail2);
        
        entityManager.flush();

        Optional<SubmissionMaxStats> maxStatsOpt = repository.findMaxStatsBySubmissionId(submission.getId());
        assertTrue(maxStatsOpt.isPresent());
        assertEquals(25, maxStatsOpt.get().getMaxTime());
        assertEquals(100, maxStatsOpt.get().getMaxMemory());
    }

    @Test
    void testCountBySubmissionId() {
        ProblemSubmissionDetailEntity detail1 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase1)
                .build();
        entityManager.persist(detail1);

        ProblemSubmissionDetailEntity detail2 = ProblemSubmissionDetailEntity.builder()
                .submission(submission)
                .testcase(testcase2)
                .build();
        entityManager.persist(detail2);
        
        entityManager.flush();

        long count = repository.countBySubmissionId(submission.getId());
        assertEquals(2, count);
    }
}
