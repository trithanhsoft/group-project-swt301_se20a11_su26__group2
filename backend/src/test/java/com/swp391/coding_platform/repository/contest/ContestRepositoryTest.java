package com.swp391.coding_platform.repository.contest;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ContestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContestRepository contestRepository;

    @Test
    void findUpcomingContests_shouldReturnCorrectContest() {
        Instant now = Instant.now();
        
        UserEntity user = new UserEntity();
        user.setUsername("contestuser");
        user.setDisplayname("Contest User");
        user.setEmail("contest@example.com");
        entityManager.persist(user);

        ContestEntity upcomingContest = ContestEntity.builder()
                .title("Upcoming Contest")
                .status(ContestStatus.PUBLISHED)
                .startTime(now.plus(1, ChronoUnit.HOURS))
                .endTime(now.plus(3, ChronoUnit.HOURS))
                .durations(120)
                .createdBy(user)
                .build();
        entityManager.persist(upcomingContest);

        ContestEntity endedContest = ContestEntity.builder()
                .title("Ended Contest")
                .status(ContestStatus.PUBLISHED)
                .startTime(now.minus(3, ChronoUnit.HOURS))
                .endTime(now.minus(1, ChronoUnit.HOURS))
                .durations(120)
                .createdBy(user)
                .build();
        entityManager.persist(endedContest);
        
        entityManager.flush();

        Page<ContestEntity> result = contestRepository.findUpcomingContests(now, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Upcoming Contest");
    }
}

