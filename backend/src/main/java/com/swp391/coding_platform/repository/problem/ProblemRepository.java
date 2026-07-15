package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Integer>, JpaSpecificationExecutor<ProblemEntity> {
    List<ProblemEntity> findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(Collection<ProblemScope> scopes);
    List<ProblemEntity> findByProblemScopeIn(Collection<ProblemScope> scopes);
    Optional<ProblemEntity> findByIdAndIsActiveTrueAndIsPublicTrue(Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE ProblemEntity p SET p.totalSubmission = p.totalSubmission + 1 WHERE p.id = :problemId")
    void incrementTotalSubmission(@Param("problemId") Integer problemId);

    @Transactional
    @Modifying
    @Query("UPDATE ProblemEntity p SET p.totalAccepted = p.totalAccepted + 1 WHERE p.id = :problemId")
    void incrementTotalAccepted(@Param("problemId") Integer problemId);
}
