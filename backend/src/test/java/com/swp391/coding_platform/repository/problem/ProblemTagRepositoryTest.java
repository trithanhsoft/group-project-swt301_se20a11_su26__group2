package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ProblemTagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemTagRepository repository;

    @BeforeEach
    void setUp() {
        ProblemTagEntity tag = ProblemTagEntity.builder()
                .name("Graph")
                .slug("graph")
                .build();
        entityManager.persist(tag);
        entityManager.flush();
    }

    @Test
    void testFindByName_Found() {
        Optional<ProblemTagEntity> result = repository.findByName("Graph");
        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("graph");
    }

    @Test
    void testFindByName_NotFound() {
        Optional<ProblemTagEntity> result = repository.findByName("Tree");
        assertThat(result).isNotPresent();
    }
}
