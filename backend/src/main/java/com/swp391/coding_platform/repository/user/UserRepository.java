package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.projection.RankingUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
        List<UserEntity> findAllByCreatedAtAfter(Instant after);
        long countByStatus(com.swp391.coding_platform.entity.enums.UserStatus status);
        @Modifying
        @Query("UPDATE UserEntity u SET u.score = COALESCE(u.score, 0) + :score WHERE u.id = :userId")
        void incrementUserScore(@Param("userId") Integer userId, @Param("score") Integer score);
        Optional<UserEntity> findByUsername(String username);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        Optional<UserEntity> findByEmail(String email);

        @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.wallet WHERE u.username = :username")
        Optional<UserEntity> findByUsernameWithWallet(@Param("username") String username);

        @Query(value = "WITH RankedUsers AS (" +
                        "    SELECT id, RANK() OVER (ORDER BY score DESC, created_at ASC) as current_rank " +
                        "    FROM users" +
                        ") " +
                        "SELECT current_rank FROM RankedUsers WHERE id = :userId", nativeQuery = true)
        Integer getUserRanking(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(DISTINCT ps.problem_id) FROM problem_submissions ps " +
                        "JOIN problems p ON ps.problem_id = p.id " +
                        "WHERE ps.user_id = :userId AND ps.verdict = 'ACCEPTED' AND p.problem_scope = 'PRACTICE'", nativeQuery = true)
        Long countSolvedPracticeProblemsByUserId(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(*) " +
                        "FROM problems " +
                        "WHERE problem_scope = 'PRACTICE' " +
                        "AND is_active = true " +
                        "AND is_public = true", nativeQuery = true)
        Long countTotalPracticeProblems();

        @Query(value = "SELECT " +
                        "  u.id as userId, " +
                        "  u.displayname as displayname, " +
                        "  u.avatarurl as avatarurl, " +
                        "  u.score as points " +
                        "FROM public.users u " +
                        "ORDER BY u.score DESC, u.id ASC", nativeQuery = true)
        List<RankingUserProjection> getGlobalRankingList();
}
