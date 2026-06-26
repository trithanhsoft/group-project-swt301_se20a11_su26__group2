package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.QuizQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, Integer> {

    @Query("SELECT DISTINCT qq FROM QuizQuestionEntity qq " +
           "LEFT JOIN FETCH qq.options qo " +
           "WHERE qq.quiz.id = :quizId " +
           "ORDER BY qq.orderIndex ASC, qo.orderIndex ASC")
    List<QuizQuestionEntity> findByQuizIdWithOptions(@Param("quizId") Integer quizId);
}
