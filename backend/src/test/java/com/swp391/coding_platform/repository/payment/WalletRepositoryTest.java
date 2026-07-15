package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    private UserEntity user;
    private WalletEntity wallet;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("testuser_wallet");
        user.setDisplayname("Test User Wallet");
        user.setEmail("test_wallet@example.com");
        entityManager.persist(user);

        wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("100.00"));
        entityManager.persist(wallet);

        entityManager.flush();
    }

    @Test
    void findByUserId_ReturnsWallet() {
        Optional<WalletEntity> found = walletRepository.findByUserId(user.getId());

        assertTrue(found.isPresent());
        assertEquals(wallet.getId(), found.get().getId());
        assertEquals(new BigDecimal("100.00"), found.get().getBalance());
    }

    @Test
    void findByUserIdWithLock_ReturnsWallet() {
        Optional<WalletEntity> found = walletRepository.findByUserIdWithLock(user.getId());

        assertTrue(found.isPresent());
        assertEquals(wallet.getId(), found.get().getId());
    }
}

