package com.swp391.coding_platform.repository.cart;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.cart.CartEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
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
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private CartEntity cart;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("testuser")
                .displayname("Test User")
                .email("test@example.com")
                .passwordHash("hash")
                .build();
        entityManager.persist(user);

        cart = new CartEntity(user);
        entityManager.persist(cart);
        entityManager.flush();
    }

    @Test
    void findByUserId_ShouldReturnCart() {
        Optional<CartEntity> found = cartRepository.findByUserId(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(cart.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_ShouldReturnEmpty_WhenUserIdDoesNotExist() {
        Optional<CartEntity> found = cartRepository.findByUserId(999);

        assertThat(found).isNotPresent();
    }
}
