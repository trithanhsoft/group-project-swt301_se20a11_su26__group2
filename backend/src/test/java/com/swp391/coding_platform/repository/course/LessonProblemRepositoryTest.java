package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.course.LessonProblemEntity;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
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
class LessonProblemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonProblemRepository lessonProblemRepository;

    private LessonEntity lesson;
    private ProblemEntity problem;
    private ProblemEntity problem2;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("instructorUser3")
                .displayname("Instructor 3")
                .email("instructor3@example.com")
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
                .title("Course 3")
                .shortDescription("Desc 3")
                .longDescription("Long Desc 3")
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

        problem = ProblemEntity.builder()
                .createdBy(user)
                .problemScope(ProblemScope.SHARED)
                .build();
        problem = entityManager.persist(problem);

        ProblemVersionEntity version = ProblemVersionEntity.builder()
                .problem(problem)
                .versionNumber(1)
                .title("Problem Title")
                .description("Problem Desc")
                .problemScope(ProblemScope.SHARED)
                .build();
        version = entityManager.persist(version);

        LessonProblemEntity lessonProblem = LessonProblemEntity.builder()
                .lesson(lesson)
                .problem(problem)
                .problemVersion(version)
                .orderIndex(1)
                .build();
        entityManager.persist(lessonProblem);
        
        problem2 = ProblemEntity.builder()
                .createdBy(user)
                .problemScope(ProblemScope.SHARED)
                .build();
        problem2 = entityManager.persist(problem2);
        
        ProblemVersionEntity version2 = ProblemVersionEntity.builder()
                .problem(problem2)
                .versionNumber(1)
                .title("Problem Title 2")
                .description("Problem Desc 2")
                .problemScope(ProblemScope.SHARED)
                .build();
        version2 = entityManager.persist(version2);

        LessonProblemEntity lessonProblem2 = LessonProblemEntity.builder()
                .lesson(lesson)
                .problem(problem2)
                .problemVersion(version2)
                .orderIndex(2)
                .build();
        entityManager.persist(lessonProblem2);

        entityManager.flush();
    }

    @Test
    void testExistsByLessonIdAndProblemId() {
        boolean exists = lessonProblemRepository.existsByLessonIdAndProblemId(lesson.getId(), problem.getId());
        assertThat(exists).isTrue();

        ProblemEntity problem3 = ProblemEntity.builder()
                .createdBy(lesson.getChapter().getCourse().getInstructor().getUser())
                .problemScope(ProblemScope.SHARED)
                .build();
        problem3 = entityManager.persistAndFlush(problem3);

        boolean notExists = lessonProblemRepository.existsByLessonIdAndProblemId(lesson.getId(), problem3.getId());
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByLessonIdOrderByOrderIndexAsc() {
        List<LessonProblemEntity> result = lessonProblemRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
        
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProblem().getId()).isEqualTo(problem.getId());
        assertThat(result.get(1).getProblem().getId()).isEqualTo(problem2.getId());
    }
}
