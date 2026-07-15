package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.LessonStatus;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class QuizAttemptAnswerRepositoryTest {

    @Autowired
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private QuizAttemptEntity quizAttempt;
    private QuizOptionEntity option;

    @BeforeEach
    void setUp() {
        UserEntity instUser = new UserEntity();
        instUser.setUsername("instructor3");
        instUser.setEmail("inst3@example.com");
        instUser.setPasswordHash("hash");
        instUser.setDisplayname("Instructor");
        instUser.setAvatarurl("url");
        instUser.setScore(0);
        instUser.setStatus(UserStatus.ACTIVE);
        instUser.setCreatedAt(Instant.now());
        instUser = entityManager.persist(instUser);

        InstructorEntity instructor = new InstructorEntity();
        instructor.setUser(instUser);
        instructor.setFullName("Instructor Name");
        instructor.setMajor("CS");
        instructor = entityManager.persist(instructor);

        CourseEntity course = new CourseEntity();
        course.setInstructor(instructor);
        course.setTitle("Java Basics");
        course.setShortDescription("Short desc");
        course.setLongDescription("Long desc");
        course.setType("FREE");
        course.setPrice(BigDecimal.ZERO);
        course.setStatus(CourseStatus.APPROVED);
        course = entityManager.persist(course);

        ChapterEntity chapter = new ChapterEntity();
        chapter.setCourse(course);
        chapter.setTitle("Chapter 1");
        chapter.setOrderIndex(1);
        chapter = entityManager.persist(chapter);

        LessonEntity lesson = new LessonEntity();
        lesson.setChapter(chapter);
        lesson.setTitle("Lesson 1");
        lesson.setIsTrial(true);
        lesson.setOrderIndex(1);
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson = entityManager.persist(lesson);

        QuizEntity quiz = new QuizEntity();
        quiz.setLesson(lesson);
        quiz.setTitle("Quiz 1");
        quiz = entityManager.persist(quiz);

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setQuiz(quiz);
        question.setContent("What is Java?");
        question.setOrderIndex(1);
        question = entityManager.persist(question);

        option = new QuizOptionEntity();
        option.setQuestion(question);
        option.setContent("A PL");
        option.setOrderIndex(1);
        option.setIsCorrect(true);
        option = entityManager.persist(option);

        quizAttempt = new QuizAttemptEntity();
        quizAttempt.setUserId(1); // just a dummy id
        quizAttempt.setQuiz(quiz);
        quizAttempt.setTotalQuestion(1);
        quizAttempt.setCorrectQuestion(1);
        quizAttempt.setScore(100.0);
        quizAttempt = entityManager.persist(quizAttempt);

        QuizAttemptAnswerEntity answer = new QuizAttemptAnswerEntity();
        answer.setQuizAttempt(quizAttempt);
        answer.setQuizQuestion(question);
        answer.setSelectedOption(option);
        entityManager.persist(answer);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByAttemptIdWithOptions() {
        List<QuizAttemptAnswerEntity> answers = quizAttemptAnswerRepository.findByAttemptIdWithOptions(quizAttempt.getId());
        
        assertFalse(answers.isEmpty());
        assertEquals(1, answers.size());
        assertNotNull(answers.get(0).getSelectedOption());
        assertEquals("A PL", answers.get(0).getSelectedOption().getContent());
    }
}
