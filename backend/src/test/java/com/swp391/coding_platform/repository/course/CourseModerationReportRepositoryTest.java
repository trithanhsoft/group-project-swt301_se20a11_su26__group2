package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.CourseModerationReportEntity;
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
class CourseModerationReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseModerationReportRepository repository;

    private CourseEntity course;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("instructorUser4")
                .displayname("Instructor 4")
                .email("instructor4@example.com")
                .build();
        user = entityManager.persist(user);

        InstructorEntity instructor = InstructorEntity.builder()
                .user(user)
                .fullName("John Doe")
                .major("Software Engineering")
                .build();
        instructor = entityManager.persist(instructor);

        course = CourseEntity.builder()
                .instructor(instructor)
                .title("Course 4")
                .shortDescription("Desc 4")
                .longDescription("Long Desc 4")
                .type("FREE")
                .build();
        course = entityManager.persist(course);

        CourseModerationReportEntity report = CourseModerationReportEntity.builder()
                .courseId(course.getId())
                .status("PENDING")
                .reportJson("{\"issues\": []}")
                .build();
        entityManager.persist(report);

        entityManager.flush();
    }

    @Test
    void testFindByCourseId() {
        Optional<CourseModerationReportEntity> report = repository.findByCourseId(course.getId());
        assertThat(report).isPresent();
        assertThat(report.get().getStatus()).isEqualTo("PENDING");

        Optional<CourseModerationReportEntity> notFound = repository.findByCourseId(999L);
        assertThat(notFound).isNotPresent();
    }
}
