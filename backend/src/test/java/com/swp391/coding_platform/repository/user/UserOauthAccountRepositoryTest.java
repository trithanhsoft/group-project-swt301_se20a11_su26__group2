package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.user.UserOauthAccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class UserOauthAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserOauthAccountRepository userOauthAccountRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .username("oauthuser")
                .displayname("OAuth User")
                .email("oauth@example.com")
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void testFindByProviderAndProviderUserId_whenExists_shouldReturnAccount() {
        // Arrange
        UserOauthAccountEntity oauthAccount = UserOauthAccountEntity.builder()
                .user(testUser)
                .provider("google")
                .providerUserId("google-id-123")
                .build();
        entityManager.persist(oauthAccount);
        entityManager.flush();

        // Act
        Optional<UserOauthAccountEntity> result = userOauthAccountRepository.findByProviderAndProviderUserId("google", "google-id-123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProvider()).isEqualTo("google");
        assertThat(result.get().getProviderUserId()).isEqualTo("google-id-123");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void testFindByProviderAndProviderUserId_whenNotExists_shouldReturnEmpty() {
        // Act
        Optional<UserOauthAccountEntity> result = userOauthAccountRepository.findByProviderAndProviderUserId("github", "github-id-456");

        // Assert
        assertThat(result).isNotPresent();
    }
}
