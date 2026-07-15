package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
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
class LessonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonRepository lessonRepository;

    private CourseEntity course1;
    private CourseEntity course2;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("instructorUser2")
                .displayname("Instructor 2")
                .email("instructor2@example.com")
                .build();
        user = entityManager.persist(user);

        InstructorEntity instructor = InstructorEntity.builder()
                .user(user)
                .fullName("John Doe")
                .major("Software Engineering")
                .build();
        instructor = entityManager.persist(instructor);

        course1 = CourseEntity.builder()
                .instructor(instructor)
                .title("Course 1")
                .shortDescription("Desc 1")
                .longDescription("Long Desc 1")
                .type("FREE")
                .build();
        course1 = entityManager.persist(course1);

        course2 = CourseEntity.builder()
                .instructor(instructor)
                .title("Course 2")
                .shortDescription("Desc 2")
                .longDescription("Long Desc 2")
                .type("PAID")
                .build();
        course2 = entityManager.persist(course2);

        ChapterEntity chapter1 = ChapterEntity.builder()
                .course(course1)
                .title("Chapter 1 C1")
                .orderIndex(1)
                .build();
        chapter1 = entityManager.persist(chapter1);

        ChapterEntity chapter2 = ChapterEntity.builder()
                .course(course2)
                .title("Chapter 1 C2")
                .orderIndex(1)
                .build();
        chapter2 = entityManager.persist(chapter2);

        LessonEntity lesson1 = LessonEntity.builder()
                .chapter(chapter1)
                .title("Lesson 1")
                .isTrial(true)
                .orderIndex(1)
                .build();
        entityManager.persist(lesson1);

        LessonEntity lesson2 = LessonEntity.builder()
                .chapter(chapter1)
                .title("Lesson 2")
                .isTrial(false)
                .orderIndex(2)
                .build();
        entityManager.persist(lesson2);

        LessonEntity lesson3 = LessonEntity.builder()
                .chapter(chapter2)
                .title("Lesson 1")
                .isTrial(true)
                .orderIndex(1)
                .build();
        entityManager.persist(lesson3);

        entityManager.flush();
    }

    @Test
    void testCountLessonsByCourseIds() {
        List<Object[]> counts = lessonRepository.countLessonsByCourseIds(List.of(course1.getId(), course2.getId()));
        
        assertThat(counts).hasSize(2);
        boolean course1Found = false;
        boolean course2Found = false;
        for (Object[] countRow : counts) {
            Long courseId = (Long) countRow[0];
            Long count = (Long) countRow[1];
            if (courseId.equals(course1.getId())) {
                assertThat(count).isEqualTo(2L);
                course1Found = true;
            } else if (courseId.equals(course2.getId())) {
                assertThat(count).isEqualTo(1L);
                course2Found = true;
            }
        }
        assertThat(course1Found).isTrue();
        assertThat(course2Found).isTrue();
    }
}
