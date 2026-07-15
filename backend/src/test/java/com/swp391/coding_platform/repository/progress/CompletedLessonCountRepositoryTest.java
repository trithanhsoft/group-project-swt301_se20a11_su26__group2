package com.swp391.coding_platform.repository.progress;

import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CompletedLessonCountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompletedLessonCountRepository completedLessonCountRepository;

    @Test
    void testGetByUserIdAndCourseId() {
        UserEntity user = new UserEntity();
        user.setEmail("count@test.com");
        user.setUsername("countuser");
        user.setPasswordHash("hash"); user.setDisplayname("test");
        user = entityManager.persistAndFlush(user);

        com.swp391.coding_platform.entity.instructor.InstructorEntity instructor = new com.swp391.coding_platform.entity.instructor.InstructorEntity();
        instructor.setUser(user);
        instructor.setFullName("Inst");
        instructor.setMajor("CS");
        entityManager.persistAndFlush(instructor);

        CourseEntity course = new CourseEntity();
        course.setTitle("Course Count Test");
        course.setShortDescription("short");
        course.setLongDescription("long");
        course.setType("PAID");
        course.setInstructor(instructor);
        course.setPrice(java.math.BigDecimal.valueOf(100.0));
        course = entityManager.persistAndFlush(course);

        CompletedLessonsCountEntity entity = new CompletedLessonsCountEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setCompletedLessonsCount(5);
        entityManager.persistAndFlush(entity);

        Optional<CompletedLessonsCountEntity> found = completedLessonCountRepository.getByUserIdAndCourseId(user.getId(), course.getId());
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getCompletedLessonsCount());
    }

    @Test
    void testFindByUserIdAndCourseIdIn() {
        UserEntity user = new UserEntity();
        user.setEmail("count2@test.com");
        user.setUsername("countuser2");
        user.setPasswordHash("hash"); user.setDisplayname("test");
        user = entityManager.persistAndFlush(user);

        com.swp391.coding_platform.entity.instructor.InstructorEntity instructor = new com.swp391.coding_platform.entity.instructor.InstructorEntity();
        instructor.setUser(user);
        instructor.setFullName("Inst");
        instructor.setMajor("CS");
        entityManager.persistAndFlush(instructor);

        CourseEntity course1 = new CourseEntity();
        course1.setTitle("Course 1");
        course1.setShortDescription("short");
        course1.setLongDescription("long");
        course1.setType("PAID");
        course1.setInstructor(instructor);
        course1.setPrice(java.math.BigDecimal.valueOf(100.0));
        course1 = entityManager.persistAndFlush(course1);

        CourseEntity course2 = new CourseEntity();
        course2.setTitle("Course 2");
        course2.setShortDescription("short");
        course2.setLongDescription("long");
        course2.setType("PAID");
        course2.setInstructor(instructor);
        course2.setPrice(java.math.BigDecimal.valueOf(100.0));
        course2 = entityManager.persistAndFlush(course2);

        CompletedLessonsCountEntity entity1 = new CompletedLessonsCountEntity();
        entity1.setUser(user);
        entity1.setCourse(course1);
        entity1.setCompletedLessonsCount(1);
        entityManager.persistAndFlush(entity1);

        List<CompletedLessonsCountEntity> results = completedLessonCountRepository.findByUserIdAndCourseIdIn(user.getId(), Set.of(course1.getId(), course2.getId()));
        assertEquals(1, results.size());
        assertEquals(course1.getId(), results.get(0).getCourse().getId());
    }

    @Test
    void testIncrementAndGetCount() {
        UserEntity user = new UserEntity();
        user.setEmail("count3@test.com");
        user.setUsername("countuser3");
        user.setPasswordHash("hash"); user.setDisplayname("test");
        user = entityManager.persistAndFlush(user);

        com.swp391.coding_platform.entity.instructor.InstructorEntity instructor = new com.swp391.coding_platform.entity.instructor.InstructorEntity();
        instructor.setUser(user);
        instructor.setFullName("Inst");
        instructor.setMajor("CS");
        entityManager.persistAndFlush(instructor);

        CourseEntity course = new CourseEntity();
        course.setTitle("Course 3");
        course.setShortDescription("short");
        course.setLongDescription("long");
        course.setType("PAID");
        course.setInstructor(instructor);
        course.setPrice(java.math.BigDecimal.valueOf(100.0));
        course = entityManager.persistAndFlush(course);

        CompletedLessonsCountEntity entity = new CompletedLessonsCountEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setCompletedLessonsCount(2);
        entityManager.persistAndFlush(entity);

        Integer newCount = completedLessonCountRepository.incrementAndGetCount(user.getId(), course.getId());
        
        // H2 might not support RETURNING clause perfectly in native queries. If it fails in H2, we can just assert it runs or ignore.
        // Wait, RETURNING is PostgreSQL specific. H2 mode might need to be compatible, or this test might fail in pure H2 without pg compat.
        // Let's check if it doesn't throw.
        assertNotNull(newCount);
        assertEquals(3, newCount);
    }
}

