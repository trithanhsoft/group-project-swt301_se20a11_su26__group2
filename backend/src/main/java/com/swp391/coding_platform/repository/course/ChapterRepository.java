package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, Integer> {

    @EntityGraph(attributePaths = {"lessons"})
    List<ChapterEntity> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    int countByCourseId(Long courseId);
}
