package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.QuizAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, Integer> {
    Optional<QuizAttemptEntity> findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(Integer userId, Integer quizId);
}
