package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblemEntity, Integer> {
    boolean existsByContestIdAndProblemId(Integer contestId, Integer problemId);

    java.util.Optional<ContestProblemEntity> findByContestIdAndProblemId(Integer contestId, Integer problemId);

    @Query("SELECT cp FROM ContestProblemEntity cp JOIN FETCH cp.problem WHERE cp.contest.id = :contestId ORDER BY cp.orderIndex ASC")
    List<ContestProblemEntity> findByContestIdWithProblem(@Param("contestId") Integer contestId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ContestProblemEntity cp WHERE cp.contest.id = :contestId")
    void deleteByContestId(@Param("contestId") Integer contestId);
}
