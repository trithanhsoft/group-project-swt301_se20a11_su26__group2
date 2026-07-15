package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestParticipantEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ContestParticipantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContestParticipantRepository contestParticipantRepository;

    private UserEntity user;
    private ContestEntity contest;
    private ProblemEntity problem;

    @BeforeEach
    public void setUp() {
        user = UserEntity.builder()
                .username("testuser_contest")
                .email("test_contest@example.com")
                .displayname("Test Contest User")
                .build();
        entityManager.persist(user);

        contest = ContestEntity.builder()
                .title("Test Contest")
                .createdBy(user)
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .durations(7200)
                .build();
        entityManager.persist(contest);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .problemScope(ProblemScope.CONTEST)
                .build();
        entityManager.persist(problem);
    }

    @Test
    public void testIsUserParticipantOfProblemContest() {
        // Setup Problem Version
        ProblemVersionEntity problemVersion = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .title("Test Problem")
                .description("Description")
                .problemScope(ProblemScope.CONTEST)
                .build();
        entityManager.persist(problemVersion);

        // Setup Contest Problem
        ContestProblemEntity contestProblem = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problemVersion)
                .orderIndex(1)
                .build();
        entityManager.persist(contestProblem);

        // Setup Participant
        ContestParticipantEntity participant = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .build();
        entityManager.persist(participant);

        entityManager.flush();

        boolean isParticipant = contestParticipantRepository.isUserParticipantOfProblemContest(user.getId(), problem.getId());
        assertThat(isParticipant).isTrue();

        boolean isNotParticipant = contestParticipantRepository.isUserParticipantOfProblemContest(user.getId(), 999);
        assertThat(isNotParticipant).isFalse();
    }

    @Test
    public void testDeleteByContestId() {
        ContestParticipantEntity participant = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .build();
        entityManager.persist(participant);
        entityManager.flush();

        contestParticipantRepository.deleteByContestId(contest.getId());
        entityManager.flush();
        entityManager.clear();

        List<ContestParticipantEntity> all = contestParticipantRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    public void testFindByUserIdWithContest() {
        ContestParticipantEntity participant = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .build();
        entityManager.persist(participant);
        entityManager.flush();

        List<ContestParticipantEntity> found = contestParticipantRepository.findByUserIdWithContest(user.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContest().getTitle()).isEqualTo("Test Contest");
    }

    @Test
    public void testCountParticipantsByContestIds() {
        ContestParticipantEntity participant1 = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .build();
        entityManager.persist(participant1);

        UserEntity user2 = UserEntity.builder()
                .username("user2")
                .email("user2@example.com")
                .displayname("User 2")
                .build();
        entityManager.persist(user2);

        ContestParticipantEntity participant2 = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user2)
                .build();
        entityManager.persist(participant2);
        entityManager.flush();

        List<Object[]> counts = contestParticipantRepository.countParticipantsByContestIds(List.of(contest.getId()));
        assertThat(counts).hasSize(1);
        assertThat(counts.get(0)[0]).isEqualTo(contest.getId());
        assertThat(((Number) counts.get(0)[1]).longValue()).isEqualTo(2L);
    }

    @Test
    public void testCountByContestId() {
        ContestParticipantEntity participant = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .build();
        entityManager.persist(participant);
        entityManager.flush();

        long count = contestParticipantRepository.countByContestId(contest.getId());
        assertThat(count).isEqualTo(1L);
    }
}
