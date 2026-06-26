package com.swp391.coding_platform.repository.instructor;

import com.swp391.coding_platform.entity.enums.InstructorAppStatus;
import com.swp391.coding_platform.entity.instructor.InstructorApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorApplicationRepository extends JpaRepository<InstructorApplicationEntity, Integer> {
    List<InstructorApplicationEntity> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    Optional<InstructorApplicationEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, InstructorAppStatus status);
    
    List<InstructorApplicationEntity> findAllByOrderByCreatedAtDesc();

    List<InstructorApplicationEntity> findByStatusAndUpdatedAtBefore(InstructorAppStatus status, java.time.Instant updatedAt);
}
