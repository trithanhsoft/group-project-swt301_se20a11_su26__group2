package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemTestcaseRepository extends JpaRepository<ProblemTestcaseEntity, Integer> {
    List<ProblemTestcaseEntity> findByProblemIdOrderByOrderIndexAsc(Integer problemId);
    List<ProblemTestcaseEntity> findByProblemIdOrderByOrderIndex(Integer problemId);
    void deleteByProblemId(Integer problemId);
}
