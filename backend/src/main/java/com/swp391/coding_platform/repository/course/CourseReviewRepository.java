package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.CourseReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReviewEntity, Integer> {

    Page<CourseReviewEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    @Query("SELECT r.star, COUNT(r) FROM CourseReviewEntity r WHERE r.course.id = :courseId GROUP BY r.star")
    List<Object[]> countStarsByCourseId(@Param("courseId") Long courseId);

    Optional<CourseReviewEntity> findByCourseIdAndUserId(Long courseId, Integer userId);
}
