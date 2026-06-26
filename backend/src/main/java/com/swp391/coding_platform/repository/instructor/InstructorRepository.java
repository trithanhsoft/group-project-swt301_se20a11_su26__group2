package com.swp391.coding_platform.repository.instructor;

import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<InstructorEntity, Integer> {
    Optional<InstructorEntity> findByUserId(Integer userId);
}
