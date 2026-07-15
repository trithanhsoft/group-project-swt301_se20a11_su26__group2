package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.course.QuizEntity;
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
class QuizRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuizRepository quizRepository;

    private LessonEntity lesson;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("instructorUser5")
                .displayname("Instructor 5")
                .email("instructor5@example.com")
                .build();
        user = entityManager.persist(user);

        InstructorEntity instructor = InstructorEntity.builder()
                .user(user)
                .fullName("John Doe")
                .major("Software Engineering")
                .build();
        instructor = entityManager.persist(instructor);

        CourseEntity course = CourseEntity.builder()
                .instructor(instructor)
                .title("Course 5")
                .shortDescription("Desc 5")
                .longDescription("Long Desc 5")
                .type("FREE")
                .build();
        course = entityManager.persist(course);

        ChapterEntity chapter = ChapterEntity.builder()
                .course(course)
                .title("Chapter 1")
                .orderIndex(1)
                .build();
        chapter = entityManager.persist(chapter);

        lesson = LessonEntity.builder()
                .chapter(chapter)
                .title("Lesson 1")
                .isTrial(true)
                .orderIndex(1)
                .build();
        lesson = entityManager.persist(lesson);

        QuizEntity quiz = QuizEntity.builder()
                .lesson(lesson)
                .title("Quiz 1")
                .build();
        entityManager.persist(quiz);

        entityManager.flush();
    }

    @Test
    void testFindByLessonId() {
        Optional<QuizEntity> result = quizRepository.findByLessonId(lesson.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Quiz 1");

        Optional<QuizEntity> notFound = quizRepository.findByLessonId(999);
        assertThat(notFound).isNotPresent();
    }
}
