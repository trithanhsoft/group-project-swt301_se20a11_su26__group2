package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.CourseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long>, JpaSpecificationExecutor<CourseEntity> {
    @Query("SELECT c.title, c.instructor.fullName, COUNT(e) FROM CourseEntity c " +
           "LEFT JOIN EnrollmentEntity e ON e.course = c " +
           "GROUP BY c.title, c.instructor.fullName " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> findTopCoursesDynamic(Pageable pageable);

    @Query("SELECT c.instructor.fullName, COUNT(e) FROM CourseEntity c " +
           "JOIN EnrollmentEntity e ON e.course = c " +
           "GROUP BY c.instructor.fullName " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> findTopInstructors(Pageable pageable);
    @EntityGraph(attributePaths = {"categories", "teacherAssignments", "teacherAssignments.teacher"})
    @Query( "SELECT c " +
            "FROM CourseEntity c " +
            "WHERE c.id = :courseId " +
                   "AND c.status = 'ACTIVE'")
    Optional<CourseEntity> findCourseDetailById(@Param("courseId") Long courseId);

    @Modifying
    @Query("UPDATE CourseEntity c SET c.totalEnrolled = c.totalEnrolled + 1 WHERE c.id = :courseId")
    void incrementTotalEnrolled(@Param("courseId") Long courseId);

    @Modifying
    @Query("UPDATE CourseEntity c SET c.totalEnrolled = c.totalEnrolled + 1 WHERE c.id IN :courseIds")
    void incrementTotalEnrolledForCourses(@Param("courseIds") List<Long> courseIds);

    List<CourseEntity> findByInstructorId(Integer instructorId);

    Optional<CourseEntity> findByIdAndInstructorId(Long id, Integer instructorId);

    boolean existsByIdAndInstructorId(Long id, Integer instructorId);

    @Query(value = "SELECT EXISTS (" +
           "SELECT 1 FROM courses c " +
           "JOIN chapters ch ON c.id = ch.course_id " +
           "JOIN lessons l ON ch.id = l.chapter_id " +
           "WHERE l.id = :lessonId AND c.instructor_id = :instructorId" +
           ")", nativeQuery = true)
    boolean existsByLessonIdAndInstructorId(@Param("lessonId") Long lessonId, @Param("instructorId") Integer instructorId);

    @Query(value = "SELECT EXISTS (" +
           "SELECT 1 FROM courses c " +
           "JOIN chapters ch ON c.id = ch.course_id " +
           "JOIN lessons l ON ch.id = l.chapter_id " +
           "JOIN quizzes q ON l.id = q.lesson_id " +
           "WHERE q.id = :quizId AND c.instructor_id = :instructorId" +
           ")", nativeQuery = true)
    boolean existsByQuizIdAndInstructorId(@Param("quizId") Long quizId, @Param("instructorId") Integer instructorId);

    @Modifying
    @Query(value = "INSERT INTO public.course_embeddings (course_id, embedding) " +
                   "VALUES (:courseId, cast(:embedding as vector)) " +
                   "ON CONFLICT (course_id) DO UPDATE SET embedding = EXCLUDED.embedding", nativeQuery = true)
    void saveCourseEmbedding(@Param("courseId") Long courseId, @Param("embedding") String embedding);

    @Query(value = "SELECT course_id, (1 - (embedding <=> cast(:newEmbedding as vector))) AS similarity " +
                   "FROM public.course_embeddings " +
                   "WHERE course_id != :currentCourseId " +
                   "ORDER BY embedding <=> cast(:newEmbedding as vector) " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findDuplicateCourses(
        @Param("newEmbedding") String newEmbedding, 
        @Param("currentCourseId") Long currentCourseId, 
        @Param("limit") int limit
    );
}


