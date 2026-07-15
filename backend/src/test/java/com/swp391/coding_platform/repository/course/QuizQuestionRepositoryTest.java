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
public class QuizQuestionRepositoryTest {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private QuizEntity quiz;

    @BeforeEach
    void setUp() {
        UserEntity instUser = new UserEntity();
        instUser.setUsername("instructor4");
        instUser.setEmail("inst4@example.com");
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

        quiz = new QuizEntity();
        quiz.setLesson(lesson);
        quiz.setTitle("Quiz 1");
        quiz = entityManager.persist(quiz);

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setQuiz(quiz);
        question.setContent("What is Java?");
        question.setOrderIndex(1);
        question = entityManager.persist(question);

        QuizOptionEntity option1 = new QuizOptionEntity();
        option1.setQuestion(question);
        option1.setContent("A PL");
        option1.setOrderIndex(1);
        option1.setIsCorrect(true);
        entityManager.persist(option1);

        QuizOptionEntity option2 = new QuizOptionEntity();
        option2.setQuestion(question);
        option2.setContent("A Car");
        option2.setOrderIndex(2);
        option2.setIsCorrect(false);
        entityManager.persist(option2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByQuizIdWithOptions() {
        List<QuizQuestionEntity> questions = quizQuestionRepository.findByQuizIdWithOptions(quiz.getId());
        
        assertFalse(questions.isEmpty());
        assertEquals(1, questions.size());
        
        QuizQuestionEntity fetchedQuestion = questions.get(0);
        assertEquals("What is Java?", fetchedQuestion.getContent());
        assertFalse(fetchedQuestion.getOptions().isEmpty());
        assertEquals(2, fetchedQuestion.getOptions().size());
        assertEquals("A PL", fetchedQuestion.getOptions().get(0).getContent());
        assertEquals("A Car", fetchedQuestion.getOptions().get(1).getContent());
    }
}
