package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipantEntity, Integer> {
    @Query("SELECT COUNT(cp) > 0 FROM ContestParticipantEntity cp " +
           "WHERE cp.user.id = :userId AND cp.contest.id IN " +
           "(SELECT cprob.contest.id FROM ContestProblemEntity cprob WHERE cprob.problem.id = :problemId)")
    boolean isUserParticipantOfProblemContest(@Param("userId") Integer userId, @Param("problemId") Integer problemId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ContestParticipantEntity cp WHERE cp.contest.id = :contestId")
    void deleteByContestId(@Param("contestId") Integer contestId);

    @Query("SELECT cp FROM ContestParticipantEntity cp JOIN FETCH cp.contest WHERE cp.user.id = :userId ORDER BY cp.joinedAt DESC")
    java.util.List<ContestParticipantEntity> findByUserIdWithContest(@Param("userId") Integer userId);

    @Query("SELECT cp.contest.id, COUNT(cp) FROM ContestParticipantEntity cp WHERE cp.contest.id IN :contestIds GROUP BY cp.contest.id")
    java.util.List<Object[]> countParticipantsByContestIds(@Param("contestIds") java.util.List<Integer> contestIds);

    long countByContestId(Integer contestId);
}
