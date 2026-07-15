package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProblemSubmissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemSubmissionRepository problemSubmissionRepository;

    @Test
    void findByUserIdAndProblemIdIn_shouldReturnSubmissions() {
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .displayname("testuser")
                .build();
        entityManager.persist(user);

        com.swp391.coding_platform.entity.problem.ProblemVersionEntity version = com.swp391.coding_platform.entity.problem.ProblemVersionEntity.builder()
                .title("Test Problem")
                .description("Test description")
                .versionNumber(1)
                .problemScope(com.swp391.coding_platform.entity.enums.ProblemScope.PRACTICE)
                .isActive(true)
                .build();

        ProblemEntity problem = ProblemEntity.builder()
                .createdBy(user)
                .versions(new java.util.ArrayList<>(java.util.List.of(version)))
                .build();
        version.setProblem(problem);
        entityManager.persist(problem);

        ProblemSubmissionEntity submission = ProblemSubmissionEntity.builder()
                .user(user)
                .problem(problem)
                .problemVersion(version)
                .languageId(71) // Python or whatever
                .sourceCode("print('hello')")
                .verdict(OjVerdict.ACCEPTED)
                .build();
        entityManager.persist(submission);

        entityManager.flush();

        List<ProblemSubmissionEntity> results = problemSubmissionRepository.findByUserIdAndProblemIdIn(user.getId(), List.of(problem.getId()));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getVerdict()).isEqualTo(OjVerdict.ACCEPTED);
    }
}

