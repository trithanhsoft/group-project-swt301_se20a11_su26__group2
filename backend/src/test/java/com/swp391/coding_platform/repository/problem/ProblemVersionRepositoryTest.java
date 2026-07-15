package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ProblemVersionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemVersionRepository problemVersionRepository;

    private UserEntity user;
    private ProblemEntity problem;

    @BeforeEach
    public void setUp() {
        user = UserEntity.builder()
                .username("testuser_pv")
                .email("testuser_pv@example.com")
                .displayname("Test User")
                .build();
        entityManager.persist(user);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .problemScope(ProblemScope.PRACTICE)
                .build();
        entityManager.persist(problem);
    }

    @Test
    public void testFindByProblemIdOrderByVersionNumberDesc() {
        ProblemVersionEntity version1 = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .title("Version 1")
                .description("Desc 1")
                .problemScope(ProblemScope.PRACTICE)
                .build();
        entityManager.persist(version1);

        ProblemVersionEntity version2 = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(2)
                .title("Version 2")
                .description("Desc 2")
                .problemScope(ProblemScope.PRACTICE)
                .build();
        entityManager.persist(version2);

        entityManager.flush();

        List<ProblemVersionEntity> versions = problemVersionRepository.findByProblemIdOrderByVersionNumberDesc(problem.getId());

        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getVersionNumber()).isEqualTo(2);
        assertThat(versions.get(1).getVersionNumber()).isEqualTo(1);
    }
}
