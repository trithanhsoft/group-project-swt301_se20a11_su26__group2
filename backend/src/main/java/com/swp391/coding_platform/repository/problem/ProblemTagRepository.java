package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemTagRepository extends JpaRepository<ProblemTagEntity, Integer> {
    Optional<ProblemTagEntity> findByName(String name);
}
