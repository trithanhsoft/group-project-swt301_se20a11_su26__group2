package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.EnrollmentEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.course.QuizEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
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
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private InstructorEntity instructor;
    private CourseEntity course;
    private EnrollmentEntity enrollment;
    private LessonEntity lesson;
    private QuizEntity quiz;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("student1");
        user.setEmail("student1@example.com");
        user.setPasswordHash("hash");
        user.setDisplayname("Student");
        user.setAvatarurl("url");
        user.setScore(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user = entityManager.persist(user);

        UserEntity instUser = new UserEntity();
        instUser.setUsername("instructor1");
        instUser.setEmail("inst1@example.com");
        instUser.setPasswordHash("hash");
        instUser.setDisplayname("Instructor");
        instUser.setAvatarurl("url");
        instUser.setScore(0);
        instUser.setStatus(UserStatus.ACTIVE);
        instUser.setCreatedAt(Instant.now());
        instUser = entityManager.persist(instUser);

        instructor = new InstructorEntity();
        instructor.setUser(instUser);
        instructor.setFullName("Instructor Name");
        instructor.setMajor("CS");
        instructor = entityManager.persist(instructor);

        course = new CourseEntity();
        course.setInstructor(instructor);
        course.setTitle("Java Basics");
        course.setShortDescription("Short desc");
        course.setLongDescription("Long desc");
        course.setType("FREE");
        course.setPrice(BigDecimal.ZERO);
        course.setStatus(CourseStatus.APPROVED);
        course = entityManager.persist(course);

        enrollment = new EnrollmentEntity();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(Instant.now());
        enrollment = entityManager.persist(enrollment);

        ChapterEntity chapter = new ChapterEntity();
        chapter.setCourse(course);
        chapter.setTitle("Chapter 1");
        chapter.setOrderIndex(1);
        chapter = entityManager.persist(chapter);

        lesson = new LessonEntity();
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
        
        entityManager.flush();
    }

    @Test
    void testCountByUserIdAndStatus() {
        Long count = enrollmentRepository.countByUserIdAndStatus(user.getId(), EnrollmentStatus.ACTIVE);
        assertEquals(1L, count);
    }

    @Test
    void testFindEnrolledCourseIdsByUserIdAndCourseIds() {
        Set<Long> ids = enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(
                user.getId(), List.of(course.getId()), List.of(EnrollmentStatus.ACTIVE));
        assertTrue(ids.contains(course.getId()));
    }

    @Test
    void testIsUserEnrolledInLesson() {
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInLesson(Long.valueOf(user.getId()), Long.valueOf(lesson.getId()));
        assertTrue(isEnrolled);
    }

    @Test
    void testIsUserEnrolledInQuiz() {
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInQuiz(Long.valueOf(user.getId()), Long.valueOf(quiz.getId()));
        assertTrue(isEnrolled);
    }

    @Test
    void testUpdateStatusByUserIdAndCourseId() {
        enrollmentRepository.updateStatusByUserIdAndCourseId(
                Long.valueOf(user.getId()), course.getId(), EnrollmentStatus.COMPLETED);
        
        entityManager.flush();
        entityManager.clear();
        
        EnrollmentEntity updated = entityManager.find(EnrollmentEntity.class, enrollment.getId());
        assertEquals(EnrollmentStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testFindActiveCoursesByUserId() {
        Set<CourseEntity> activeCourses = enrollmentRepository.findActiveCoursesByUserId(Long.valueOf(user.getId()));
        assertFalse(activeCourses.isEmpty());
        assertEquals(course.getId(), activeCourses.iterator().next().getId());
    }

    @Test
    void testFindEnrollmentsByInstructorId() {
        List<EnrollmentEntity> enrollments = enrollmentRepository.findEnrollmentsByInstructorId(instructor.getId());
        assertFalse(enrollments.isEmpty());
        assertEquals(enrollment.getId(), enrollments.get(0).getId());
    }

    @Test
    void testFindEnrollmentWithLock() {
        Optional<EnrollmentEntity> lockedEnrollment = enrollmentRepository.findEnrollmentWithLock(user.getId(), course.getId());
        assertTrue(lockedEnrollment.isPresent());
        assertEquals(enrollment.getId(), lockedEnrollment.get().getId());
    }
}
