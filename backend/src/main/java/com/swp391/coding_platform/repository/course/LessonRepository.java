package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Integer> {

    @Query("SELECT l.chapter.course.id, COUNT(l) FROM LessonEntity l WHERE l.chapter.course.id IN :courseIds GROUP BY l.chapter.course.id")
    List<Object[]> countLessonsByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}
