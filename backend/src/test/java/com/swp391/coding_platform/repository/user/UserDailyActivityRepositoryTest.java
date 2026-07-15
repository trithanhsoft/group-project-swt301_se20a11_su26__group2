package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserDailyActivityEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class UserDailyActivityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserDailyActivityRepository repository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("testuser")
                .displayname("Test User")
                .email("testuser@example.com")
                .build();
        user = entityManager.persistAndFlush(user);
    }

    @Test
    void testTrackActivity_newActivity() {
        repository.trackActivity(user.getId());
        entityManager.clear();

        List<UserDailyActivityEntity> activities = repository.findAll();
        assertEquals(1, activities.size());
        assertEquals(user.getId(), activities.get(0).getUser().getId());
        assertEquals(LocalDate.now(), activities.get(0).getActivityDate());
        assertEquals(1, activities.get(0).getStreak());
    }

    @Test
    void testFindActiveDatesByYear() {
        UserDailyActivityEntity activity1 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2023, 5, 10))
                .streak(1)
                .build();
        UserDailyActivityEntity activity2 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2023, 6, 15))
                .streak(2)
                .build();
        UserDailyActivityEntity activity3 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2024, 1, 1))
                .streak(1)
                .build();

        entityManager.persist(activity1);
        entityManager.persist(activity2);
        entityManager.persist(activity3);
        entityManager.flush();

        List<LocalDate> dates2023 = repository.findActiveDatesByYear(user.getId(), 2023);
        assertEquals(2, dates2023.size());
        assertTrue(dates2023.contains(LocalDate.of(2023, 5, 10)));
        assertTrue(dates2023.contains(LocalDate.of(2023, 6, 15)));

        List<LocalDate> dates2024 = repository.findActiveDatesByYear(user.getId(), 2024);
        assertEquals(1, dates2024.size());
        assertTrue(dates2024.contains(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void testGetMaxStreak() {
        UserDailyActivityEntity activity1 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2023, 5, 10))
                .streak(1)
                .build();
        UserDailyActivityEntity activity2 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2023, 5, 11))
                .streak(2)
                .build();
        UserDailyActivityEntity activity3 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.of(2023, 6, 1))
                .streak(5) // some higher max streak
                .build();

        entityManager.persist(activity1);
        entityManager.persist(activity2);
        entityManager.persist(activity3);
        entityManager.flush();

        Integer maxStreak = repository.getMaxStreak(user.getId());
        assertEquals(5, maxStreak);
    }

    @Test
    void testGetCurrentValidStreak_today() {
        UserDailyActivityEntity activity1 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.now())
                .streak(3)
                .build();
        entityManager.persistAndFlush(activity1);

        Number currentStreak = repository.getCurrentValidStreak(user.getId());
        assertNotNull(currentStreak);
        assertEquals(3, currentStreak.intValue());
    }

    @Test
    void testGetCurrentValidStreak_yesterday() {
        UserDailyActivityEntity activity1 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.now().minusDays(1))
                .streak(4)
                .build();
        entityManager.persistAndFlush(activity1);

        Number currentStreak = repository.getCurrentValidStreak(user.getId());
        assertNotNull(currentStreak);
        assertEquals(4, currentStreak.intValue());
    }

    @Test
    void testGetCurrentValidStreak_older() {
        UserDailyActivityEntity activity1 = UserDailyActivityEntity.builder()
                .user(user)
                .activityDate(LocalDate.now().minusDays(2))
                .streak(4)
                .build();
        entityManager.persistAndFlush(activity1);

        Number currentStreak = repository.getCurrentValidStreak(user.getId());
        assertNull(currentStreak);
    }
}
