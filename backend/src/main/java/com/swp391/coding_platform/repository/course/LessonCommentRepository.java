package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.LessonCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonCommentRepository extends JpaRepository<LessonCommentEntity, Integer> {

    @Query("SELECT DISTINCT c FROM LessonCommentEntity c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.replies r " +
           "LEFT JOIN FETCH r.user " +
           "WHERE c.lesson.id = :lessonId AND c.parent IS NULL " +
           "ORDER BY c.createdAt DESC")
    List<LessonCommentEntity> findRootCommentsWithRepliesAndUsers(@Param("lessonId") Integer lessonId);
}
