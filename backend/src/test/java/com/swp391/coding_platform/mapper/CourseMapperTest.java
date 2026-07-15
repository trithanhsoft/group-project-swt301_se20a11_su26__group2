package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import com.swp391.coding_platform.entity.enums.LessonStatus;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CourseMapperTest {

    private final CourseMapper mapper = new CourseMapperImpl();

    @Test
    void toCourseListItemResponse() {
        CourseEntity course = new CourseEntity();
        InstructorEntity instructor = new InstructorEntity();
        instructor.setFullName("John Doe");
        course.setInstructor(instructor);

        CourseListItemResponse response = mapper.toCourseListItemResponse(course);
        assertEquals("John Doe", response.getInstructorName());
    }

    @Test
    void toCourseDetailResponse() {
        CourseEntity course = new CourseEntity();
        InstructorEntity instructor = new InstructorEntity();
        instructor.setFullName("John Doe");
        instructor.setMajor("Software Engineer");
        instructor.setBio("Hello");
        UserEntity user = new UserEntity();
        user.setAvatarurl("avatar.jpg");
        instructor.setUser(user);
        course.setInstructor(instructor);

        CategoryEntity category = new CategoryEntity();
        category.setName("Programming");
        course.setCategories(Set.of(category));

        CourseDetailResponse response = mapper.toCourseDetailResponse(course);
        assertEquals("John Doe", response.getInstructorName());
        assertEquals("Software Engineer", response.getInstructorTitle());
        assertEquals("Hello", response.getInstructorBio());
        assertEquals("avatar.jpg", response.getInstructorAvatarUrl());
        assertEquals("Programming", response.getCategoryName());
    }

    @Test
    void toCurriculumLessonResponse_VideoTrial() {
        LessonEntity lesson = new LessonEntity();
        lesson.setIsTrial(true);
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setVideoUrl("http://video.com");
        
        CurriculumLessonResponse response = mapper.toCurriculumLessonResponse(lesson);
        assertEquals("http://video.com", response.getVideoUrl());
        assertEquals("video", response.getType());
    }

    @Test
    void toCurriculumLessonResponse_NotTrial() {
        LessonEntity lesson = new LessonEntity();
        lesson.setIsTrial(false);
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setVideoUrl("http://video.com");

        CurriculumLessonResponse response = mapper.toCurriculumLessonResponse(lesson);
        assertNull(response.getVideoUrl());
        assertEquals("video", response.getType());
    }

    @Test
    void toCurriculumLessonResponse_ReadingType() {
        LessonEntity lesson = new LessonEntity();
        lesson.setTheoryContent("Theory content");
        
        CurriculumLessonResponse response = mapper.toCurriculumLessonResponse(lesson);
        assertEquals("reading", response.getType());
    }

    @Test
    void toCourseReviewDto() {
        CourseReviewEntity review = new CourseReviewEntity();
        UserEntity user = new UserEntity();
        user.setDisplayname("Reviewer");
        user.setAvatarurl("avatar.jpg");
        review.setUser(user);

        CourseReviewDto dto = mapper.toCourseReviewDto(review);
        assertEquals("Reviewer", dto.getDisplayName());
        assertEquals("avatar.jpg", dto.getAvatarUrl());
    }

    @Test
    void toLearningDetailResponse() {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Title");
        InstructorEntity instructor = new InstructorEntity();
        instructor.setFullName("John");
        course.setInstructor(instructor);

        LessonEntity activeLesson = new LessonEntity();
        activeLesson.setId(10);
        activeLesson.setTitle("Lesson Title");
        activeLesson.setStatus(LessonStatus.ACTIVE);
        activeLesson.setVideoUrl("video.mp4");

        LearningDetailResponse response = mapper.toLearningDetailResponse(course, 50, activeLesson);
        assertEquals(1L, response.getCourseId());
        assertEquals("Title", response.getCourseTitle());
        assertEquals("John", response.getInstructorName());
        assertEquals(50, response.getProgressPercentage());
        assertEquals(10, response.getActiveLessonId());
        assertEquals("video.mp4", response.getActiveLessonVideoUrl());
    }

    @Test
    void toLearningDetailResponse_InactiveLesson() {
        CourseEntity course = new CourseEntity();
        LessonEntity activeLesson = new LessonEntity();
        activeLesson.setStatus(LessonStatus.INACTIVE);
        activeLesson.setVideoUrl("video.mp4");

        LearningDetailResponse response = mapper.toLearningDetailResponse(course, 0, activeLesson);
        assertNull(response.getActiveLessonVideoUrl());
    }

    @Test
    void toLearningLessonResponse() {
        LessonEntity lesson = new LessonEntity();
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setVideoUrl("vid");
        lesson.setTheoryContent("theory");
        lesson.setLessonProblems(new java.util.ArrayList<>());

        LearningLessonResponse response = mapper.toLearningLessonResponse(lesson);
        assertEquals("vid", response.getVideoUrl());
        assertEquals("theory", response.getTheoryContent());
    }

    @Test
    void toLearningLessonResponse_Inactive() {
        LessonEntity lesson = new LessonEntity();
        lesson.setStatus(LessonStatus.INACTIVE);
        lesson.setVideoUrl("vid");
        lesson.setLessonProblems(new java.util.ArrayList<>());

        LearningLessonResponse response = mapper.toLearningLessonResponse(lesson);
        assertNull(response.getVideoUrl());
        assertNull(response.getExercises());
    }

    @Test
    void toLearningExerciseResponse_DifficultyColors() {
        LessonProblemEntity lessonProblem = new LessonProblemEntity();
        ProblemEntity problem = new ProblemEntity();
        problem.setId(1);
        problem.setTotalSubmission(100);
        lessonProblem.setProblem(problem);

        ProblemVersionEntity version = new ProblemVersionEntity();
        version.setTitle("Title");

        // EASY
        version.setDifficulty(ProblemDifficulty.EASY);
        lessonProblem.setProblemVersion(version);
        LearningExerciseResponse r1 = mapper.toLearningExerciseResponse(lessonProblem);
        assertTrue(r1.getDifficultyClass().contains("green"));

        // MEDIUM
        version.setDifficulty(ProblemDifficulty.MEDIUM);
        lessonProblem.setProblemVersion(version);
        LearningExerciseResponse r2 = mapper.toLearningExerciseResponse(lessonProblem);
        assertTrue(r2.getDifficultyClass().contains("yellow"));

        // HARD
        version.setDifficulty(ProblemDifficulty.HARD);
        lessonProblem.setProblemVersion(version);
        LearningExerciseResponse r3 = mapper.toLearningExerciseResponse(lessonProblem);
        assertTrue(r3.getDifficultyClass().contains("red"));
    }

    @Test
    void toLearningCurriculumLessonResponse() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(5);
        lesson.setVideoUrl("vid");

        Set<Integer> completed = Set.of(5, 6);
        LearningCurriculumLessonResponse response = mapper.toLearningCurriculumLessonResponse(lesson, completed);
        assertTrue(response.getIsCompleted());
        assertEquals("video", response.getType());
    }
}
