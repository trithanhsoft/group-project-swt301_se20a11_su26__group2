package com.swp391.coding_platform.repository.progress;

import com.swp391.coding_platform.entity.progress.LessonProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgressEntity, Long> {

      @Query("SELECT lp.lessonId " +
                  "FROM LessonProgressEntity lp " +
                  "WHERE lp.course.id = :courseId " +
                  "AND lp.user.id = :userId")
      Set<Integer> findCompletedLessonIds(@Param("userId") Integer userId,
                  @Param("courseId") Long courseId);

      Boolean existsByLessonIdAndUserId(Integer lessonId, Integer userId);

}

