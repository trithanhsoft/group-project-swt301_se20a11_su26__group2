package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemVisualizerCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemVisualizerCacheRepository extends JpaRepository<ProblemVisualizerCache, String> {
    Optional<ProblemVisualizerCache> findByProblemIdAndUserIdAndPromptVersion(String problemId, String userId, Integer promptVersion);
}
