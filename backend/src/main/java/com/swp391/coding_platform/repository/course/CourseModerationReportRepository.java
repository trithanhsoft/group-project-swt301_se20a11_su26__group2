package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.CourseModerationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourseModerationReportRepository extends JpaRepository<CourseModerationReportEntity, Long> {
    
    // Tìm kiếm báo cáo kiểm duyệt theo courseId
    Optional<CourseModerationReportEntity> findByCourseId(Long courseId);
}
