package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class QuizAttemptRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private UserEntity user;
    private QuizEntity quiz;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("quizstudent")
                .displayname("Quiz Student")
                .email("quizstudent@example.com")
                .build();
        user = entityManager.persist(user);

        UserEntity instructorUser = UserEntity.builder()
                .username("quizinstructor")
                .displayname("Quiz Instructor")
                .email("quizinstructor@example.com")
                .build();
        instructorUser = entityManager.persist(instructorUser);

        InstructorEntity instructor = InstructorEntity.builder()
                .user(instructorUser)
                .fullName("Jane Smith")
                .major("Computer Science")
                .build();
        instructor = entityManager.persist(instructor);

        CourseEntity course = CourseEntity.builder()
                .instructor(instructor)
                .title("Advanced Algorithms")
                .shortDescription("Short Description")
                .longDescription("Long Description")
                .type("PAID")
                .build();
        course = entityManager.persist(course);

        ChapterEntity chapter = ChapterEntity.builder()
                .course(course)
                .title("Chapter 1: Sorting")
                .orderIndex(1)
                .build();
        chapter = entityManager.persist(chapter);

        LessonEntity lesson = LessonEntity.builder()
                .chapter(chapter)
                .title("Lesson 1: QuickSort")
                .isTrial(false)
                .orderIndex(1)
                .build();
        lesson = entityManager.persist(lesson);

        quiz = QuizEntity.builder()
                .lesson(lesson)
                .title("Quiz 1: QuickSort logic")
                .build();
        quiz = entityManager.persist(quiz);

        entityManager.flush();
    }

    @Test
    void testFindTopByUserIdAndQuizIdOrderBySubmittedAtDesc_Success() {
        // Create multiple attempts at different times
        QuizAttemptEntity attempt1 = QuizAttemptEntity.builder()
                .userId(user.getId())
                .quiz(quiz)
                .totalQuestion(10)
                .correctQuestion(6)
                .score(6.0)
                .submittedAt(Instant.now().minusSeconds(3600)) // 1 hour ago
                .build();

        QuizAttemptEntity attempt2 = QuizAttemptEntity.builder()
                .userId(user.getId())
                .quiz(quiz)
                .totalQuestion(10)
                .correctQuestion(9)
                .score(9.0)
                .submittedAt(Instant.now()) // Now
                .build();

        QuizAttemptEntity attempt3 = QuizAttemptEntity.builder()
                .userId(user.getId())
                .quiz(quiz)
                .totalQuestion(10)
                .correctQuestion(8)
                .score(8.0)
                .submittedAt(Instant.now().minusSeconds(1800)) // 30 mins ago
                .build();

        entityManager.persist(attempt1);
        entityManager.persist(attempt2);
        entityManager.persist(attempt3);
        entityManager.flush();

        // Should return attempt2 since it has the latest submittedAt
        Optional<QuizAttemptEntity> result = quizAttemptRepository.findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(user.getId(), quiz.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getCorrectQuestion()).isEqualTo(9);
        assertThat(result.get().getScore()).isEqualTo(9.0);
    }

    @Test
    void testFindTopByUserIdAndQuizIdOrderBySubmittedAtDesc_NotFound() {
        Optional<QuizAttemptEntity> result = quizAttemptRepository.findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(999, quiz.getId());
        assertThat(result).isNotPresent();
    }
}
