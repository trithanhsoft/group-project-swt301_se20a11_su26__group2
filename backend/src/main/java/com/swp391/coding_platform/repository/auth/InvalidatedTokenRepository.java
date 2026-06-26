package com.swp391.coding_platform.repository.auth;

import com.swp391.coding_platform.entity.auth.InvalidatedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedTokenEntity, Integer> {
    boolean existsByTokenJti(String tokenJti);
}
