package com.swp391.coding_platform.repository.instructor;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class InstructorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InstructorRepository instructorRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .username("instructorUser")
                .displayname("Instructor Display Name")
                .email("instructor@example.com")
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void testFindByUserId_whenInstructorExists_shouldReturnInstructor() {
        // Arrange
        InstructorEntity instructor = InstructorEntity.builder()
                .user(testUser)
                .fullName("John Doe")
                .major("Computer Science")
                .bio("Experienced Software Engineer")
                .build();
        entityManager.persist(instructor);
        entityManager.flush();

        // Act
        Optional<InstructorEntity> result = instructorRepository.findByUserId(testUser.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
        assertThat(result.get().getMajor()).isEqualTo("Computer Science");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void testFindByUserId_whenInstructorDoesNotExist_shouldReturnEmpty() {
        // Act
        Optional<InstructorEntity> result = instructorRepository.findByUserId(testUser.getId());

        // Assert
        assertThat(result).isNotPresent();
    }
}
