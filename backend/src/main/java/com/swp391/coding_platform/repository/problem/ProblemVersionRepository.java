package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemVersionRepository extends JpaRepository<ProblemVersionEntity, Integer> {
    List<ProblemVersionEntity> findByProblemIdOrderByVersionNumberDesc(Integer problemId);
}
