package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestRankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContestRankingRepository extends JpaRepository<ContestRankingEntity, Integer> {

    @Query("SELECT r FROM ContestRankingEntity r WHERE r.contest.id = :contestId AND r.user.id = :userId")
    Optional<ContestRankingEntity> findByContestIdAndUserId(
            @Param("contestId") Integer contestId,
            @Param("userId") Integer userId);

    @Query("SELECT COUNT(cr) FROM ContestRankingEntity cr WHERE cr.contest.id = :contestId AND " +
           "(cr.problemsSolved > :solved OR (cr.problemsSolved = :solved AND cr.totalPenalty < :penalty))")
    long countBetterRankings(
            @Param("contestId") Integer contestId,
            @Param("solved") Integer solved,
            @Param("penalty") Integer penalty);

    @Query("SELECT r FROM ContestRankingEntity r WHERE r.user.id = :userId AND r.contest.id IN :contestIds")
    java.util.List<ContestRankingEntity> findByUserIdAndContestIds(
            @Param("userId") Integer userId,
            @Param("contestIds") java.util.List<Integer> contestIds);
}
