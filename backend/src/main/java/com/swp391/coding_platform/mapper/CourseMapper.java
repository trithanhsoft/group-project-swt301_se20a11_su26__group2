package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.course.CourseReviewEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "enrolled", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "instructorName", source = "instructor.fullName")
    CourseListItemResponse toCourseListItemResponse(CourseEntity courseEntity);

    @Mapping(target = "enrolled", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "instructorName", source = "instructor.fullName")
    @Mapping(target = "instructorTitle", source = "instructor.major")
    @Mapping(target = "instructorBio", source = "instructor.bio")
    @Mapping(target = "instructorAvatarUrl", source = "instructor.user.avatarurl")
    @Mapping(target = "categoryName", expression = "java(courseEntity.getCategories() != null && !courseEntity.getCategories().isEmpty() ? courseEntity.getCategories().iterator().next().getName() : null)")
    CourseDetailResponse toCourseDetailResponse(CourseEntity courseEntity);

    CurriculumChapterResponse toCurriculumChapterResponse(ChapterEntity chapterEntity);

    @Mapping(target = "videoUrl", expression = "java(lessonEntity.getIsTrial() != null && lessonEntity.getIsTrial() && lessonEntity.getStatus() != com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE && lessonEntity.getStatus() != com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE ? lessonEntity.getVideoUrl() : null)")
    @Mapping(target = "type", expression = "java(lessonEntity.getVideoUrl() != null && !lessonEntity.getVideoUrl().isEmpty() ? \"video\" : (lessonEntity.getTheoryContent() != null && !lessonEntity.getTheoryContent().isEmpty() ? \"reading\" : \"coding\"))")
    CurriculumLessonResponse toCurriculumLessonResponse(LessonEntity lessonEntity);

    @Mapping(target = "displayName", source = "user.displayname")
    @Mapping(target = "avatarUrl", source = "user.avatarurl")
    CourseReviewDto toCourseReviewDto(CourseReviewEntity reviewEntity);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "instructorName", source = "course.instructor.fullName")
    @Mapping(target = "progressPercentage", source = "progressPercentage")
    @Mapping(target = "activeLessonId", source = "activeLesson.id")
    @Mapping(target = "activeLessonTitle", source = "activeLesson.title")
    @Mapping(target = "activeLessonVideoUrl", expression = "java(activeLesson != null && (activeLesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE || activeLesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE) ? null : (activeLesson != null ? activeLesson.getVideoUrl() : null))")
    @Mapping(target = "activeLessonTheoryContent", expression = "java(activeLesson != null && (activeLesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE || activeLesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE) ? null : (activeLesson != null ? activeLesson.getTheoryContent() : null))")
    LearningDetailResponse toLearningDetailResponse(CourseEntity course, int progressPercentage, LessonEntity activeLesson);

    @Mapping(target = "exercises", expression = "java(lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE || lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE ? null : lessonEntity.getLessonProblems().stream().map(this::toLearningExerciseResponse).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "videoUrl", expression = "java(lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE || lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE ? null : lessonEntity.getVideoUrl())")
    @Mapping(target = "theoryContent", expression = "java(lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE || lessonEntity.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE ? null : lessonEntity.getTheoryContent())")
    LearningLessonResponse toLearningLessonResponse(LessonEntity lessonEntity);

    @Mapping(target = "id", source = "problem.id")
    @Mapping(target = "name", source = "problem.title")
    @Mapping(target = "difficulty", source = "problem.difficulty")
    @Mapping(target = "difficultyClass", expression = "java(lessonProblemEntity.getProblem().getDifficulty() != null ? (lessonProblemEntity.getProblem().getDifficulty().name().equals(\"EASY\") ? \"bg-green-100 text-green-700 border-green-200\" : lessonProblemEntity.getProblem().getDifficulty().name().equals(\"MEDIUM\") ? \"bg-yellow-100 text-yellow-700 border-yellow-200\" : \"bg-red-100 text-red-700 border-red-200\") : \"bg-gray-100 text-gray-700\")")
    @Mapping(target = "submissions", source = "problem.totalSubmission")
    @Mapping(target = "completed", constant = "false")
    LearningExerciseResponse toLearningExerciseResponse(com.swp391.coding_platform.entity.course.LessonProblemEntity lessonProblemEntity);

    @Mapping(target = "isCompleted", expression = "java(completedLessonIds != null && completedLessonIds.contains(lessonEntity.getId()))")
    @Mapping(target = "type", expression = "java(lessonEntity.getVideoUrl() != null && !lessonEntity.getVideoUrl().isEmpty() ? \"video\" : (lessonEntity.getTheoryContent() != null && !lessonEntity.getTheoryContent().isEmpty() ? \"reading\" : \"coding\"))")
    LearningCurriculumLessonResponse toLearningCurriculumLessonResponse(LessonEntity lessonEntity, @Context Set<Integer> completedLessonIds);

    LearningCurriculumChapterResponse toLearningCurriculumChapterResponse(ChapterEntity chapterEntity, @Context Set<Integer> completedLessonIds);

    List<LearningCurriculumChapterResponse> toLearningCurriculumChapterResponses(List<ChapterEntity> chapterEntities, @Context Set<Integer> completedLessonIds);

    @Mapping(target = "exercises", source = "lessonProblems")
    com.swp391.coding_platform.dto.response.InstructorLessonResponse toInstructorLessonResponse(LessonEntity lessonEntity);

    @Mapping(target = "id", source = "problem.id")
    @Mapping(target = "title", source = "problem.title")
    @Mapping(target = "difficulty", source = "problem.difficulty")
    @Mapping(target = "description", source = "problem.description")
    @Mapping(target = "inputDesc", source = "problem.inputDescription")
    @Mapping(target = "outputDesc", source = "problem.outputDescription")
    @Mapping(target = "constraints", source = "problem.constraints")
    @Mapping(target = "exampleInput", source = "problem.exampleInput")
    @Mapping(target = "exampleOutput", source = "problem.exampleOutput")
    @Mapping(target = "hint", source = "problem.hint")
    @Mapping(target = "score", expression = "java(lessonProblemEntity.getProblem().getScore() != null ? lessonProblemEntity.getProblem().getScore().intValue() : 100)")
    @Mapping(target = "timeLimit", source = "problem.timeLimitMs")
    @Mapping(target = "memoryLimit", source = "problem.memoryLimitKb")
    @Mapping(target = "initialCode", source = "problem.starterTemplates")
    @Mapping(target = "solutionCode", source = "problem.solutions")
    @Mapping(target = "testCases", source = "problem.testcases")
    com.swp391.coding_platform.dto.response.InstructorExerciseResponse toInstructorExerciseResponse(com.swp391.coding_platform.entity.course.LessonProblemEntity lessonProblemEntity);

    @Mapping(target = "input", source = "inputData")
    @Mapping(target = "output", source = "expectedOutput")
    com.swp391.coding_platform.dto.response.InstructorTestcaseResponse toInstructorTestcaseResponse(com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity testcaseEntity);

    com.swp391.coding_platform.dto.response.InstructorQuizOptionResponse toInstructorQuizOptionResponse(com.swp391.coding_platform.entity.course.QuizOptionEntity optionEntity);
    com.swp391.coding_platform.dto.response.InstructorQuizQuestionResponse toInstructorQuizQuestionResponse(com.swp391.coding_platform.entity.course.QuizQuestionEntity questionEntity);
    com.swp391.coding_platform.dto.response.InstructorQuizResponse toInstructorQuizResponse(com.swp391.coding_platform.entity.course.QuizEntity quizEntity);

    com.swp391.coding_platform.dto.response.InstructorChapterResponse toInstructorChapterResponse(ChapterEntity chapterEntity);

    @Mapping(target = "topic", source = "type")
    com.swp391.coding_platform.dto.response.InstructorCourseDetailResponse toInstructorCourseDetailResponse(CourseEntity courseEntity);

    com.swp391.coding_platform.dto.response.InstructorCourseDetailResponse.CategoryDto toCategoryDto(com.swp391.coding_platform.entity.category.CategoryEntity categoryEntity);
}
