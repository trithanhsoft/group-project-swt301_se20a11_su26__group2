package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PaymentTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    private UserEntity user;
    private WalletEntity wallet;
    private PaymentTransactionEntity transaction;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("testuser");
        user.setDisplayname("Test User");
        user.setEmail("test@example.com");
        entityManager.persist(user);

        wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("100.00"));
        entityManager.persist(wallet);

        transaction = new PaymentTransactionEntity();
        transaction.setWallet(wallet);
        transaction.setTransactionCode("TXN123456");
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setType(PaymentType.DEPOSIT);
        transaction.setStatus(StatusTransaction.PENDING);
        entityManager.persist(transaction);

        entityManager.flush();
    }

    @Test
    void findByTransactionCode_ReturnsTransaction() {
        Optional<PaymentTransactionEntity> found = paymentTransactionRepository.findByTransactionCode("TXN123456");

        assertTrue(found.isPresent());
        assertEquals(transaction.getId(), found.get().getId());
    }

    @Test
    void findByWalletUserId_ReturnsTransactionsPage() {
        Page<PaymentTransactionEntity> page = paymentTransactionRepository.findByWalletUserId(user.getId(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("TXN123456", page.getContent().get(0).getTransactionCode());
    }

    @Test
    void findByWalletUserIdAndType_ReturnsTransactionsPage() {
        Page<PaymentTransactionEntity> page = paymentTransactionRepository.findByWalletUserIdAndType(user.getId(), PaymentType.DEPOSIT, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(PaymentType.DEPOSIT, page.getContent().get(0).getType());
    }

    @Test
    void findByStatusAndCreatedAtBefore_ReturnsTransactions() {
        Instant limit = Instant.now().plus(1, ChronoUnit.HOURS);
        List<PaymentTransactionEntity> list = paymentTransactionRepository.findByStatusAndCreatedAtBefore(StatusTransaction.PENDING, limit);

        assertFalse(list.isEmpty());
        assertEquals("TXN123456", list.get(0).getTransactionCode());
    }
}

