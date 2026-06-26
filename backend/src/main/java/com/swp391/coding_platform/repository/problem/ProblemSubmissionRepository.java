package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmissionEntity, Integer> {
    @Query("SELECT p.title, p.difficulty, COUNT(ps) FROM ProblemSubmissionEntity ps " +
           "JOIN ps.problem p " +
           "GROUP BY p.title, p.difficulty " +
           "ORDER BY COUNT(ps) DESC")
    List<Object[]> findTopProblems(Pageable pageable);
    long countByUserIdAndProblemIdAndVerdict(Integer userId, Integer problemId, OjVerdict verdict);

    List<ProblemSubmissionEntity> findByUserIdAndProblemId(Integer userId, Integer problemId);

    List<ProblemSubmissionEntity> findByUserId(Integer userId);

    List<ProblemSubmissionEntity> findByUserIdAndProblemIdIn(Integer userId, List<Integer> problemIds);

    @Query("SELECT p FROM ProblemSubmissionEntity p JOIN FETCH p.problem WHERE p.user.id = :userId ORDER BY p.submittedAt DESC")
    List<ProblemSubmissionEntity> findSubmissionsWithProblemByUserId(@Param("userId") Integer userId);

    @Query("SELECT p.verdict, COUNT(p.id) " +
           "FROM ProblemSubmissionEntity p " +
           "WHERE p.user.id = :userId " +
           "GROUP BY p.verdict")
    List<Object[]> countVerdictsByUserId(@Param("userId") Integer userId);

    long countByContestId(Integer contestId);

    @Query("SELECT AVG(ps.score) FROM ProblemSubmissionEntity ps WHERE ps.contest.id = :contestId")
    Double getAverageScoreByContestId(@Param("contestId") Integer contestId);

    @Query("SELECT ps FROM ProblemSubmissionEntity ps " +
           "JOIN FETCH ps.user " +
           "JOIN FETCH ps.problem " +
           "WHERE ps.contest.id = :contestId " +
           "ORDER BY ps.submittedAt DESC")
    List<ProblemSubmissionEntity> findByContestId(@Param("contestId") Integer contestId);

    @Query("SELECT ps FROM ProblemSubmissionEntity ps " +
           "JOIN FETCH ps.user " +
           "JOIN FETCH ps.problem " +
           "WHERE ps.contest.id = :contestId AND ps.user.id = :userId " +
           "ORDER BY ps.submittedAt DESC")
    List<ProblemSubmissionEntity> findByContestIdAndUserId(@Param("contestId") Integer contestId, @Param("userId") Integer userId);
}
