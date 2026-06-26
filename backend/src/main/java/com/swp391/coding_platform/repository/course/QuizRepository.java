package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<QuizEntity, Integer> {
    Optional<QuizEntity> findByLessonId(Integer lessonId);
}
