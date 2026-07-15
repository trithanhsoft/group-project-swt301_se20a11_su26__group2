package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.problem.ProblemVisualizerCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ProblemVisualizerCacheRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemVisualizerCacheRepository problemVisualizerCacheRepository;

    @Test
    public void testFindByProblemIdAndUserIdAndPromptVersion() {
        ProblemVisualizerCache cache = ProblemVisualizerCache.builder()
                .problemId("prob-123")
                .userId("user-456")
                .promptVersion(1)
                .detectedAlgorithm("DFS")
                .timeComplexity("O(N)")
                .htmlContent("<html></html>")
                .generatedAt(Instant.now())
                .build();
        
        entityManager.persist(cache);
        entityManager.flush();

        Optional<ProblemVisualizerCache> result = problemVisualizerCacheRepository.findByProblemIdAndUserIdAndPromptVersion("prob-123", "user-456", 1);
        
        assertThat(result).isPresent();
        assertThat(result.get().getDetectedAlgorithm()).isEqualTo("DFS");
        assertThat(result.get().getTimeComplexity()).isEqualTo("O(N)");

        Optional<ProblemVisualizerCache> emptyResult = problemVisualizerCacheRepository.findByProblemIdAndUserIdAndPromptVersion("prob-999", "user-456", 1);
        assertThat(emptyResult).isEmpty();
    }
}
