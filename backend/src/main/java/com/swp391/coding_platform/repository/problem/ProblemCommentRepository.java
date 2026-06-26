package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemCommentRepository extends JpaRepository<ProblemCommentEntity, Integer> {
    List<ProblemCommentEntity> findByProblemIdAndParentIsNullOrderByCreatedAtDesc(Integer problemId);
    void deleteByProblemId(Integer problemId);
}
