package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestProblemAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemAttemptRepository extends JpaRepository<ContestProblemAttemptEntity, Integer> {

    @Query("SELECT a FROM ContestProblemAttemptEntity a WHERE a.contest.id = :contestId AND a.user.id = :userId")
    List<ContestProblemAttemptEntity> findByContestIdAndUserId(@Param("contestId") Integer contestId, @Param("userId") Integer userId);

    @Query("SELECT a FROM ContestProblemAttemptEntity a WHERE a.contest.id = :contestId AND a.user.id = :userId AND a.problem.id = :problemId")
    Optional<ContestProblemAttemptEntity> findByContestIdAndUserIdAndProblemId(@Param("contestId") Integer contestId, @Param("userId") Integer userId, @Param("problemId") Integer problemId);
}
