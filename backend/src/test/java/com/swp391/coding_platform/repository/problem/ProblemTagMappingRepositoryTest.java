package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity;
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
public class ProblemTagMappingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemTagMappingRepository repository;

    private ProblemEntity problem1;
    private ProblemEntity problem2;
    private ProblemTagEntity tag;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("testuser_tags")
                .email("tags@example.com")
                .displayname("Tag User")
                .build();
        entityManager.persist(user);

        problem1 = ProblemEntity.builder().createdBy(user).build();
        entityManager.persist(problem1);

        problem2 = ProblemEntity.builder().createdBy(user).build();
        entityManager.persist(problem2);

        tag = ProblemTagEntity.builder()
                .name("Dynamic Programming")
                .slug("dynamic-programming")
                .build();
        entityManager.persist(tag);

        ProblemTagMappingEntity mapping1 = ProblemTagMappingEntity.builder()
                .problem(problem1)
                .tag(tag)
                .build();
        entityManager.persist(mapping1);

        ProblemTagMappingEntity mapping2 = ProblemTagMappingEntity.builder()
                .problem(problem2)
                .tag(tag)
                .build();
        entityManager.persist(mapping2);
        
        entityManager.flush();
    }

    @Test
    void testFindByProblemId() {
        List<ProblemTagMappingEntity> mappings = repository.findByProblemId(problem1.getId());
        assertThat(mappings).hasSize(1);
        assertThat(mappings.get(0).getProblem().getId()).isEqualTo(problem1.getId());
    }

    @Test
    void testFindByProblemIdIn() {
        List<ProblemTagMappingEntity> mappings = repository.findByProblemIdIn(List.of(problem1.getId(), problem2.getId()));
        assertThat(mappings).hasSize(2);
    }

    @Test
    void testDeleteByProblemId() {
        repository.deleteByProblemId(problem1.getId());
        entityManager.flush();
        
        List<ProblemTagMappingEntity> mappings = repository.findByProblemId(problem1.getId());
        assertThat(mappings).isEmpty();
        
        List<ProblemTagMappingEntity> remaining = repository.findAll();
        assertThat(remaining).hasSize(1); // problem2 mapping should still exist
    }
}
