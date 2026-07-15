package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ChapterRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChapterRepository chapterRepository;

    private CourseEntity course;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("instructorUser1")
                .displayname("Instructor 1")
                .email("instructor1@example.com")
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
                .title("Java Basics")
                .shortDescription("Learn Java basics")
                .longDescription("Long description for Java basics")
                .type("FREE")
                .build();
        course = entityManager.persist(course);

        ChapterEntity chapter1 = ChapterEntity.builder()
                .course(course)
                .title("Chapter 1")
                .orderIndex(1)
                .build();
        entityManager.persist(chapter1);

        ChapterEntity chapter2 = ChapterEntity.builder()
                .course(course)
                .title("Chapter 2")
                .orderIndex(2)
                .build();
        entityManager.persist(chapter2);

        entityManager.flush();
    }

    @Test
    void testFindByCourseIdOrderByOrderIndexAsc() {
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
        assertThat(chapters).hasSize(2);
        assertThat(chapters.get(0).getTitle()).isEqualTo("Chapter 1");
        assertThat(chapters.get(1).getTitle()).isEqualTo("Chapter 2");
    }

    @Test
    void testCountByCourseId() {
        int count = chapterRepository.countByCourseId(course.getId());
        assertThat(count).isEqualTo(2);
    }
}
