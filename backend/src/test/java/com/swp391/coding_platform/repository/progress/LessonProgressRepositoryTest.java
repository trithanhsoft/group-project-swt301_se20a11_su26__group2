package com.swp391.coding_platform.repository.progress;

import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.progress.LessonProgressEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class LessonProgressRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Test
    void testFindCompletedLessonIds() {
        // Setup User
        UserEntity user = new UserEntity();
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPasswordHash("hash"); user.setDisplayname("test");
        user = entityManager.persistAndFlush(user);

        com.swp391.coding_platform.entity.instructor.InstructorEntity instructor = new com.swp391.coding_platform.entity.instructor.InstructorEntity();
        instructor.setUser(user);
        instructor.setFullName("Inst");
        instructor.setMajor("CS");
        entityManager.persistAndFlush(instructor);

        // Setup Course
        CourseEntity course = new CourseEntity();
        course.setTitle("Course Test");
        course.setShortDescription("short");
        course.setLongDescription("long");
        course.setType("PAID");
        course.setInstructor(instructor);
        course.setPrice(java.math.BigDecimal.valueOf(100.0));
        course = entityManager.persistAndFlush(course);

        // Setup LessonProgress
        LessonProgressEntity progress1 = new LessonProgressEntity();
        progress1.setUser(user);
        progress1.setCourse(course);
        progress1.setLessonId(10);
        entityManager.persistAndFlush(progress1);

        LessonProgressEntity progress2 = new LessonProgressEntity();
        progress2.setUser(user);
        progress2.setCourse(course);
        progress2.setLessonId(20);
        entityManager.persistAndFlush(progress2);

        Set<Integer> completedLessonIds = lessonProgressRepository.findCompletedLessonIds(user.getId(), course.getId());
        assertEquals(2, completedLessonIds.size());
        assertTrue(completedLessonIds.contains(10));
        assertTrue(completedLessonIds.contains(20));
    }

    @Test
    void testExistsByLessonIdAndUserId() {
        UserEntity user = new UserEntity();
        user.setEmail("test2@test.com");
        user.setUsername("testuser2");
        user.setPasswordHash("hash"); user.setDisplayname("test");
        user = entityManager.persistAndFlush(user);

        com.swp391.coding_platform.entity.instructor.InstructorEntity instructor = new com.swp391.coding_platform.entity.instructor.InstructorEntity();
        instructor.setUser(user);
        instructor.setFullName("Inst");
        instructor.setMajor("CS");
        entityManager.persistAndFlush(instructor);

        CourseEntity course = new CourseEntity();
        course.setTitle("Course Test 2");
        course.setShortDescription("short");
        course.setLongDescription("long");
        course.setType("PAID");
        course.setInstructor(instructor);
        course.setPrice(java.math.BigDecimal.valueOf(100.0));
        course = entityManager.persistAndFlush(course);

        LessonProgressEntity progress = new LessonProgressEntity();
        progress.setUser(user);
        progress.setCourse(course);
        progress.setLessonId(30);
        entityManager.persistAndFlush(progress);

        assertTrue(lessonProgressRepository.existsByLessonIdAndUserId(30, user.getId()));
        assertFalse(lessonProgressRepository.existsByLessonIdAndUserId(40, user.getId()));
    }
}

