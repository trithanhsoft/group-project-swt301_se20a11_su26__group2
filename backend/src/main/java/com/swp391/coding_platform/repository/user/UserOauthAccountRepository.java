package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserOauthAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOauthAccountRepository extends JpaRepository<UserOauthAccountEntity, Integer> {
    Optional<UserOauthAccountEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
