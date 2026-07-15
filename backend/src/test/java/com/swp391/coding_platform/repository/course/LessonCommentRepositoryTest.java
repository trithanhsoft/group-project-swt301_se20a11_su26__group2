package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.LessonCommentEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
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
public class LessonCommentRepositoryTest {

    @Autowired
    private LessonCommentRepository lessonCommentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private LessonEntity lesson;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("commentuser");
        user.setEmail("commentuser@example.com");
        user.setPasswordHash("hash");
        user.setDisplayname("Comment User");
        user.setAvatarurl("url");
        user.setScore(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user = entityManager.persist(user);

        UserEntity instUser = new UserEntity();
        instUser.setUsername("instructor2");
        instUser.setEmail("inst2@example.com");
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

        lesson = new LessonEntity();
        lesson.setChapter(chapter);
        lesson.setTitle("Lesson 1");
        lesson.setIsTrial(true);
        lesson.setOrderIndex(1);
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson = entityManager.persist(lesson);
    }

    @Test
    void testFindRootCommentsWithRepliesAndUsers() {
        LessonCommentEntity rootComment = new LessonCommentEntity();
        rootComment.setLesson(lesson);
        rootComment.setUser(user);
        rootComment.setContent("This is a root comment");
        rootComment.setCreatedAt(Instant.now().minusSeconds(10));
        rootComment = entityManager.persist(rootComment);

        LessonCommentEntity reply1 = new LessonCommentEntity();
        reply1.setLesson(lesson);
        reply1.setUser(user);
        reply1.setContent("This is a reply");
        reply1.setParent(rootComment);
        reply1.setCreatedAt(Instant.now());
        entityManager.persist(reply1);

        entityManager.flush();
        entityManager.clear();

        List<LessonCommentEntity> results = lessonCommentRepository.findRootCommentsWithRepliesAndUsers(lesson.getId());

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("This is a root comment", results.get(0).getContent());
        assertFalse(results.get(0).getReplies().isEmpty());
        assertEquals("This is a reply", results.get(0).getReplies().get(0).getContent());
    }
}
