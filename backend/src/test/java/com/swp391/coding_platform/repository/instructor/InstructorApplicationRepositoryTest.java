package com.swp391.coding_platform.repository.instructor;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.enums.InstructorAppStatus;
import com.swp391.coding_platform.entity.instructor.InstructorApplicationEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class InstructorApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InstructorApplicationRepository repository;

    private UserEntity testUser1;
    private UserEntity testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = UserEntity.builder()
                .username("user1")
                .displayname("User One")
                .email("user1@example.com")
                .build();
        entityManager.persist(testUser1);

        testUser2 = UserEntity.builder()
                .username("user2")
                .displayname("User Two")
                .email("user2@example.com")
                .build();
        entityManager.persist(testUser2);

        entityManager.flush();
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() throws InterruptedException {
        // Arrange
        InstructorApplicationEntity app1 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv1.pdf")
                .introduction("Intro 1")
                .build();
        entityManager.persist(app1);
        
        Thread.sleep(10); // Ensure different created_at
        
        InstructorApplicationEntity app2 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv2.pdf")
                .introduction("Intro 2")
                .build();
        entityManager.persist(app2);

        InstructorApplicationEntity app3 = InstructorApplicationEntity.builder()
                .user(testUser2)
                .cvUrl("cv3.pdf")
                .introduction("Intro 3")
                .build();
        entityManager.persist(app3);
        entityManager.flush();

        // Act
        List<InstructorApplicationEntity> results = repository.findByUserIdOrderByCreatedAtDesc(testUser1.getId());

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCvUrl()).isEqualTo("cv2.pdf"); // Newer
        assertThat(results.get(1).getCvUrl()).isEqualTo("cv1.pdf"); // Older
    }

    @Test
    void testFindFirstByUserIdAndStatusOrderByCreatedAtDesc() throws InterruptedException {
        // Arrange
        InstructorApplicationEntity app1 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv1.pdf")
                .introduction("Intro 1")
                .status(InstructorAppStatus.PENDING)
                .build();
        entityManager.persist(app1);
        
        Thread.sleep(10);
        
        InstructorApplicationEntity app2 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv2.pdf")
                .introduction("Intro 2")
                .status(InstructorAppStatus.PENDING)
                .build();
        entityManager.persist(app2);
        entityManager.flush();

        // Act
        Optional<InstructorApplicationEntity> result = repository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(testUser1.getId(), InstructorAppStatus.PENDING);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCvUrl()).isEqualTo("cv2.pdf");
    }

    @Test
    void testFindAllByOrderByCreatedAtDesc() throws InterruptedException {
        // Arrange
        InstructorApplicationEntity app1 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv1.pdf")
                .introduction("Intro 1")
                .build();
        entityManager.persist(app1);
        
        Thread.sleep(10);
        
        InstructorApplicationEntity app2 = InstructorApplicationEntity.builder()
                .user(testUser2)
                .cvUrl("cv2.pdf")
                .introduction("Intro 2")
                .build();
        entityManager.persist(app2);
        entityManager.flush();

        // Act
        List<InstructorApplicationEntity> results = repository.findAllByOrderByCreatedAtDesc();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCvUrl()).isEqualTo("cv2.pdf");
        assertThat(results.get(1).getCvUrl()).isEqualTo("cv1.pdf");
    }

    @Test
    void testFindByStatusAndUpdatedAtBefore() {
        // Arrange
        Instant now = Instant.now();
        
        InstructorApplicationEntity app1 = InstructorApplicationEntity.builder()
                .user(testUser1)
                .cvUrl("cv1.pdf")
                .introduction("Intro 1")
                .status(InstructorAppStatus.PENDING)
                .updatedAt(now.minus(2, ChronoUnit.DAYS))
                .build();
        entityManager.persist(app1);
        
        InstructorApplicationEntity app2 = InstructorApplicationEntity.builder()
                .user(testUser2)
                .cvUrl("cv2.pdf")
                .introduction("Intro 2")
                .status(InstructorAppStatus.PENDING)
                .updatedAt(now)
                .build();
        entityManager.persist(app2);
        entityManager.flush();

        // Act
        List<InstructorApplicationEntity> results = repository.findByStatusAndUpdatedAtBefore(
                InstructorAppStatus.PENDING, now.minus(1, ChronoUnit.DAYS));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCvUrl()).isEqualTo("cv1.pdf");
    }
}
