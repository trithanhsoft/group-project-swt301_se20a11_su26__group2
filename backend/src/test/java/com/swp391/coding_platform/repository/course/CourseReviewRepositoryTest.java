package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.CourseReviewEntity;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class CourseReviewRepositoryTest {

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setDisplayname("Test User");
        user.setAvatarurl("url");
        user.setScore(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user = entityManager.persist(user);

        UserEntity instructorUser = new UserEntity();
        instructorUser.setUsername("instructor");
        instructorUser.setEmail("inst@example.com");
        instructorUser.setPasswordHash("hash");
        instructorUser.setDisplayname("Inst User");
        instructorUser.setAvatarurl("url");
        instructorUser.setScore(0);
        instructorUser.setStatus(UserStatus.ACTIVE);
        instructorUser.setCreatedAt(Instant.now());
        instructorUser = entityManager.persist(instructorUser);

        InstructorEntity instructor = new InstructorEntity();
        instructor.setUser(instructorUser);
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
    }

    @Test
    void testFindByCourseIdOrderByCreatedAtDesc() {
        CourseReviewEntity review1 = new CourseReviewEntity();
        review1.setCourse(course);
        review1.setUser(user);
        review1.setContent("Good");
        review1.setStar(5);
        review1.setCreatedAt(Instant.now().minusSeconds(10));
        entityManager.persist(review1);

        CourseReviewEntity review2 = new CourseReviewEntity();
        review2.setCourse(course);
        review2.setUser(user);
        review2.setContent("Excellent");
        review2.setStar(4);
        review2.setCreatedAt(Instant.now());
        entityManager.persist(review2);
        
        entityManager.flush();
        entityManager.clear();

        Page<CourseReviewEntity> result = courseReviewRepository.findByCourseIdOrderByCreatedAtDesc(course.getId(), PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Excellent", result.getContent().get(0).getContent());
    }

    @Test
    void testCountStarsByCourseId() {
        CourseReviewEntity review1 = new CourseReviewEntity();
        review1.setCourse(course);
        review1.setUser(user);
        review1.setContent("Good");
        review1.setStar(5);
        entityManager.persist(review1);

        CourseReviewEntity review2 = new CourseReviewEntity();
        review2.setCourse(course);
        review2.setUser(user);
        review2.setContent("Ok");
        review2.setStar(5);
        entityManager.persist(review2);

        CourseReviewEntity review3 = new CourseReviewEntity();
        review3.setCourse(course);
        review3.setUser(user);
        review3.setContent("Bad");
        review3.setStar(1);
        entityManager.persist(review3);
        
        entityManager.flush();
        entityManager.clear();

        List<Object[]> results = courseReviewRepository.countStarsByCourseId(course.getId());
        
        assertNotNull(results);
        assertEquals(2, results.size());
        
        boolean found5 = false;
        boolean found1 = false;
        
        for (Object[] row : results) {
            Integer star = (Integer) row[0];
            Long count = (Long) row[1];
            if (star == 5 && count == 2L) found5 = true;
            if (star == 1 && count == 1L) found1 = true;
        }
        
        assertTrue(found5);
        assertTrue(found1);
    }

    @Test
    void testFindByCourseIdAndUserId() {
        CourseReviewEntity review = new CourseReviewEntity();
        review.setCourse(course);
        review.setUser(user);
        review.setContent("Good");
        review.setStar(5);
        entityManager.persistAndFlush(review);
        entityManager.clear();

        Optional<CourseReviewEntity> found = courseReviewRepository.findByCourseIdAndUserId(course.getId(), user.getId());
        
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getStar());
    }
}
