package com.swp391.coding_platform.repository.specification;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class CourseSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private InstructorEntity instructor1;
    private InstructorEntity instructor2;
    private CategoryEntity categoryJava;
    private CategoryEntity categoryPython;

    private CourseEntity course1;
    private CourseEntity course2;
    private CourseEntity course3;

    @BeforeEach
    void setUp() {
        // Create Instructors
        UserEntity user1 = UserEntity.builder()
                .username("teacher1")
                .displayname("Teacher One")
                .email("teacher1@example.com")
                .build();
        user1 = entityManager.persist(user1);

        instructor1 = InstructorEntity.builder()
                .user(user1)
                .fullName("Albert Einstein")
                .major("Physics")
                .build();
        instructor1 = entityManager.persist(instructor1);

        UserEntity user2 = UserEntity.builder()
                .username("teacher2")
                .displayname("Teacher Two")
                .email("teacher2@example.com")
                .build();
        user2 = entityManager.persist(user2);

        instructor2 = InstructorEntity.builder()
                .user(user2)
                .fullName("Alan Turing")
                .major("Mathematics")
                .build();
        instructor2 = entityManager.persist(instructor2);

        // Create Categories
        categoryJava = CategoryEntity.builder()
                .name("Java Programming")
                .description("Java description")
                .build();
        categoryJava = entityManager.persist(categoryJava);

        categoryPython = CategoryEntity.builder()
                .name("Python Programming")
                .description("Python description")
                .build();
        categoryPython = entityManager.persist(categoryPython);

        // Create Courses
        course1 = CourseEntity.builder()
                .instructor(instructor1)
                .title("Complete Java Guide")
                .shortDescription("Learn Java coding from scratch")
                .longDescription("Long Java description")
                .status(CourseStatus.APPROVED)
                .price(BigDecimal.valueOf(100))
                .averageRating(4.8)
                .type("FREE")
                .categories(Set.of(categoryJava))
                .build();
        course1 = entityManager.persist(course1);

        course2 = CourseEntity.builder()
                .instructor(instructor2)
                .title("Advanced Python Guide")
                .shortDescription("Master Python scripting")
                .longDescription("Long Python description")
                .status(CourseStatus.APPROVED)
                .price(BigDecimal.valueOf(250))
                .averageRating(4.2)
                .type("PAID")
                .categories(Set.of(categoryPython))
                .build();
        course2 = entityManager.persist(course2);

        course3 = CourseEntity.builder()
                .instructor(instructor1)
                .title("Draft Physics course")
                .shortDescription("Introduction to Quantum Mechanics")
                .longDescription("Long Quantum description")
                .status(CourseStatus.DRAFTS)
                .price(BigDecimal.valueOf(50))
                .averageRating(3.5)
                .type("PAID")
                .categories(Set.of())
                .build();
        course3 = entityManager.persist(course3);

        entityManager.flush();
    }

    @Test
    void testIsStatusActive() {
        Specification<CourseEntity> spec = CourseSpecification.isStatusActive();
        List<CourseEntity> results = courseRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).contains(course1, course2);
        assertThat(results).doesNotContain(course3);
    }

    @Test
    void testHasKeyword() {
        // Keyword match in title
        Specification<CourseEntity> spec1 = CourseSpecification.hasKeyword("Java");
        List<CourseEntity> results1 = courseRepository.findAll(spec1);
        assertThat(results1).hasSize(1);
        assertThat(results1.get(0).getTitle()).contains("Java");

        // Keyword match in shortDescription
        Specification<CourseEntity> spec2 = CourseSpecification.hasKeyword("scripting");
        List<CourseEntity> results2 = courseRepository.findAll(spec2);
        assertThat(results2).hasSize(1);
        assertThat(results2.get(0).getTitle()).contains("Python");

        // Keyword is null or empty
        Specification<CourseEntity> specNull = CourseSpecification.hasKeyword(null);
        assertThat(courseRepository.findAll(specNull)).hasSize(3);
    }

    @Test
    void testHasCategories() {
        Specification<CourseEntity> spec = CourseSpecification.hasCategories(List.of(categoryJava.getId().longValue()));
        List<CourseEntity> results = courseRepository.findAll(spec);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Complete Java Guide");

        Specification<CourseEntity> specEmpty = CourseSpecification.hasCategories(new ArrayList<>());
        assertThat(courseRepository.findAll(specEmpty)).hasSize(3);
    }

    @Test
    void testHasPriceBetween() {
        // Between 80 and 200
        Specification<CourseEntity> specBetween = CourseSpecification.hasPriceBetween(BigDecimal.valueOf(80), BigDecimal.valueOf(200));
        List<CourseEntity> results1 = courseRepository.findAll(specBetween);
        assertThat(results1).hasSize(1);
        assertThat(results1.get(0).getTitle()).isEqualTo("Complete Java Guide");

        // Greater than or equal to 100
        Specification<CourseEntity> specMin = CourseSpecification.hasPriceBetween(BigDecimal.valueOf(100), null);
        List<CourseEntity> results2 = courseRepository.findAll(specMin);
        assertThat(results2).hasSize(2);
        assertThat(results2).contains(course1, course2);

        // Less than or equal to 60
        Specification<CourseEntity> specMax = CourseSpecification.hasPriceBetween(null, BigDecimal.valueOf(60));
        List<CourseEntity> results3 = courseRepository.findAll(specMax);
        assertThat(results3).hasSize(1);
        assertThat(results3.get(0).getTitle()).isEqualTo("Draft Physics course");
    }

    @Test
    void testHasRatingBetween() {
        Specification<CourseEntity> spec = CourseSpecification.hasRatingBetween(4.0, 5.0);
        List<CourseEntity> results = courseRepository.findAll(spec);
        assertThat(results).hasSize(2);
        assertThat(results).contains(course1, course2);
    }

    @Test
    void testHasTeacherName() {
        Specification<CourseEntity> spec = CourseSpecification.hasTeacherName("turing");
        List<CourseEntity> results = courseRepository.findAll(spec);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Advanced Python Guide");
    }
}
