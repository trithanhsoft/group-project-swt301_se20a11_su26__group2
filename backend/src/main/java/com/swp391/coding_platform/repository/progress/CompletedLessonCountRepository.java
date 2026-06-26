package com.swp391.coding_platform.repository.progress;

import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CompletedLessonCountRepository extends JpaRepository<CompletedLessonsCountEntity, Long> {
    Optional<CompletedLessonsCountEntity> getByUserIdAndCourseId(Integer userId, Long courseId);

    List<CompletedLessonsCountEntity> findByUserIdAndCourseIdIn(Integer userId, Set<Long> courseIds);

    List<CompletedLessonsCountEntity> findByCourseId(Long courseId);

    @Query(value = "UPDATE completed_lessons_count " +
                   "SET completed_lessons_count = completed_lessons_count + 1, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE user_id = :userId AND course_id = :courseId " +
                   "RETURNING completed_lessons_count",
           nativeQuery = true)
    Integer incrementAndGetCount(@Param("userId") Integer userId, @Param("courseId") Long courseId);
}

