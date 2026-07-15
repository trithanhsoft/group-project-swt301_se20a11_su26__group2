package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestRankingEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ContestRankingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContestRankingRepository repository;

    private UserEntity user;
    private ContestEntity contest;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("ranking_testuser")
                .displayname("Test User")
                .email("ranking_testuser@example.com")
                .build();
        user = entityManager.persistAndFlush(user);

        contest = ContestEntity.builder()
                .createdBy(user)
                .title("Ranking Test Contest")
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .durations(7200)
                .build();
        contest = entityManager.persistAndFlush(contest);
    }

    @Test
    void testFindByContestIdAndUserId() {
        ContestRankingEntity ranking = ContestRankingEntity.builder()
                .contest(contest)
                .user(user)
                .problemsSolved(2)
                .totalPenalty(100)
                .build();
        entityManager.persistAndFlush(ranking);

        Optional<ContestRankingEntity> result = repository.findByContestIdAndUserId(contest.getId(), user.getId());
        assertTrue(result.isPresent());
        assertEquals(contest.getId(), result.get().getContest().getId());
        assertEquals(user.getId(), result.get().getUser().getId());
        assertEquals(2, result.get().getProblemsSolved());
        assertEquals(100, result.get().getTotalPenalty());
    }

    @Test
    void testCountBetterRankings() {
        UserEntity user2 = UserEntity.builder().username("user2").displayname("User 2").email("user2@example.com").build();
        user2 = entityManager.persistAndFlush(user2);
        
        UserEntity user3 = UserEntity.builder().username("user3").displayname("User 3").email("user3@example.com").build();
        user3 = entityManager.persistAndFlush(user3);

        // Current user ranking
        ContestRankingEntity ranking1 = ContestRankingEntity.builder().contest(contest).user(user).problemsSolved(2).totalPenalty(100).build();
        entityManager.persist(ranking1);
        
        // Better ranking (more solved)
        ContestRankingEntity ranking2 = ContestRankingEntity.builder().contest(contest).user(user2).problemsSolved(3).totalPenalty(200).build();
        entityManager.persist(ranking2);

        // Better ranking (same solved, less penalty)
        ContestRankingEntity ranking3 = ContestRankingEntity.builder().contest(contest).user(user3).problemsSolved(2).totalPenalty(50).build();
        entityManager.persist(ranking3);
        
        entityManager.flush();

        long betterCount = repository.countBetterRankings(contest.getId(), 2, 100);
        assertEquals(2, betterCount); // Both user2 and user3 are better
    }

    @Test
    void testFindByUserIdAndContestIds() {
        ContestEntity contest2 = ContestEntity.builder()
                .createdBy(user)
                .title("Contest 2")
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .durations(7200)
                .build();
        contest2 = entityManager.persistAndFlush(contest2);

        ContestRankingEntity ranking1 = ContestRankingEntity.builder().contest(contest).user(user).problemsSolved(1).totalPenalty(10).build();
        ContestRankingEntity ranking2 = ContestRankingEntity.builder().contest(contest2).user(user).problemsSolved(3).totalPenalty(50).build();
        
        entityManager.persist(ranking1);
        entityManager.persist(ranking2);
        entityManager.flush();

        List<Integer> contestIds = Arrays.asList(contest.getId(), contest2.getId());
        List<ContestRankingEntity> results = repository.findByUserIdAndContestIds(user.getId(), contestIds);
        
        assertEquals(2, results.size());
    }
}
