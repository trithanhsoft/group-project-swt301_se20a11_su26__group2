package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
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
public class ProblemTestcaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemTestcaseRepository repository;

    private ProblemVersionEntity version;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("testuser_tc")
                .email("tc@example.com")
                .displayname("TC User")
                .build();
        entityManager.persist(user);

        ProblemEntity problem = ProblemEntity.builder().createdBy(user).build();
        entityManager.persist(problem);

        version = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .title("A + B")
                .description("Calculate A + B")
                .problemScope(ProblemScope.PRACTICE)
                .build();
        entityManager.persist(version);

        ProblemTestcaseEntity testcase2 = ProblemTestcaseEntity.builder()
                .problemVersion(version)
                .inputData("2 3")
                .expectedOutput("5")
                .orderIndex(2)
                .build();
        entityManager.persist(testcase2);

        ProblemTestcaseEntity testcase1 = ProblemTestcaseEntity.builder()
                .problemVersion(version)
                .inputData("1 1")
                .expectedOutput("2")
                .orderIndex(1)
                .build();
        entityManager.persist(testcase1);

        entityManager.flush();
    }

    @Test
    void testFindByProblemVersionIdOrderByOrderIndexAsc() {
        List<ProblemTestcaseEntity> testcases = repository.findByProblemVersionIdOrderByOrderIndexAsc(version.getId());
        
        assertThat(testcases).hasSize(2);
        assertThat(testcases.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(testcases.get(1).getOrderIndex()).isEqualTo(2);
    }

    @Test
    void testFindByProblemVersionIdOrderByOrderIndex() {
        List<ProblemTestcaseEntity> testcases = repository.findByProblemVersionIdOrderByOrderIndex(version.getId());
        
        assertThat(testcases).hasSize(2);
        assertThat(testcases.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(testcases.get(1).getOrderIndex()).isEqualTo(2);
    }

    @Test
    void testDeleteByProblemVersionId() {
        repository.deleteByProblemVersionId(version.getId());
        entityManager.flush();
        
        List<ProblemTestcaseEntity> testcases = repository.findAll();
        assertThat(testcases).isEmpty();
    }
}
