package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionDetailEntity;
import com.swp391.coding_platform.repository.projection.SubmissionMaxStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemSubmissionDetailRepository extends JpaRepository<ProblemSubmissionDetailEntity, Integer> {
    List<ProblemSubmissionDetailEntity> findBySubmissionId(Integer submissionId);

    @Query("SELECT d " +
            "FROM ProblemSubmissionDetailEntity d " +
            "JOIN FETCH d.submission s " +
            "JOIN FETCH s.problem " +
            "WHERE d.token = :token")
    Optional<ProblemSubmissionDetailEntity> findByTokenWithSubmissionAndProblem(@Param("token") String token);

    Optional<ProblemSubmissionDetailEntity> findFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc(
            Integer submissionId,
            OjVerdict verdict
    );

    @Query("SELECT MAX(d.executionTime) as maxTime, " +
            "MAX(d.memoryUsed) as maxMemory " +
            "FROM ProblemSubmissionDetailEntity d " +
            "WHERE d.submission.id = :submissionId")
    Optional<SubmissionMaxStats> findMaxStatsBySubmissionId(@Param("submissionId") Integer submissionId);

    long countBySubmissionId(Integer submissionId);
}
