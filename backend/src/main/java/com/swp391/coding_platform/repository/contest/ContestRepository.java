package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<ContestEntity, Integer> {
    @Query("SELECT COUNT(c) FROM ContestEntity c WHERE c.status = com.swp391.coding_platform.entity.enums.ContestStatus.PUBLISHED AND c.startTime <= :now AND c.endTime >= :now")
    long countActiveContests(@Param("now") java.time.Instant now);

    @Query("SELECT c, " +
           "(SELECT COUNT(p) FROM ContestParticipantEntity p WHERE p.contest.id = c.id), " +
           "(SELECT COUNT(cp) FROM ContestProblemEntity cp WHERE cp.contest.id = c.id) " +
           "FROM ContestEntity c WHERE " +
           "c.status = com.swp391.coding_platform.entity.enums.ContestStatus.PUBLISHED AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:statusFilter = 'All' OR " +
           " (:statusFilter = 'Ongoing' AND c.startTime <= :now AND c.endTime >= :now) OR " +
           " (:statusFilter = 'Upcoming' AND c.startTime > :now) OR " +
           " (:statusFilter = 'Ended' AND c.endTime < :now)) AND " +
           "(:access = 'All' OR " +
           " (:access = 'Public' AND (c.passwordHash IS NULL OR c.passwordHash = '')) OR " +
           " (:access = 'Lock' AND c.passwordHash IS NOT NULL AND c.passwordHash <> ''))")
    Page<Object[]> searchContestsWithStats(
            @Param("search") String search,
            @Param("statusFilter") String statusFilter,
            @Param("now") java.time.Instant now,
            @Param("access") String access,
            Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM ContestParticipantEntity p WHERE p.contest.id = :contestId AND p.user.id = :userId")
    boolean isUserRegistered(@Param("contestId") Integer contestId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(p) FROM ContestParticipantEntity p WHERE p.contest.id = :contestId")
    long countParticipants(@Param("contestId") Integer contestId);

    @Query("SELECT COUNT(p) FROM ContestParticipantEntity p WHERE p.user.id = :userId")
    long countUserContests(@Param("userId") Integer userId);

    @Query("SELECT COUNT(cp) FROM ContestProblemEntity cp WHERE cp.contest.id = :contestId")
    long countProblems(@Param("contestId") Integer contestId);

    @Query("SELECT c FROM ContestEntity c WHERE c.status = com.swp391.coding_platform.entity.enums.ContestStatus.PUBLISHED AND c.startTime <= :now AND c.endTime >= :now ORDER BY c.startTime DESC")
    Page<ContestEntity> findOngoingContests(@Param("now") java.time.Instant now, Pageable pageable);

    @Query("SELECT c FROM ContestEntity c WHERE c.status = com.swp391.coding_platform.entity.enums.ContestStatus.PUBLISHED AND c.startTime > :now ORDER BY c.startTime ASC")
    Page<ContestEntity> findUpcomingContests(@Param("now") java.time.Instant now, Pageable pageable);

    @Query("SELECT c, " +
           "(SELECT COUNT(p) FROM ContestParticipantEntity p WHERE p.contest.id = c.id), " +
           "(SELECT COUNT(cp) FROM ContestProblemEntity cp WHERE cp.contest.id = c.id), " +
           "(SELECT COUNT(ps) FROM ProblemSubmissionEntity ps WHERE ps.contest.id = c.id), " +
           "(SELECT AVG(ps.score) FROM ProblemSubmissionEntity ps WHERE ps.contest.id = c.id) " +
           "FROM ContestEntity c ORDER BY c.createdAt DESC")
    List<Object[]> getAdminContestsWithStats();
}
