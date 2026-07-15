package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class WalletTransactionRepositoryTest {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private WalletEntity wallet;
    private WalletTransactionEntity txSuccess;
    private WalletTransactionEntity txPending;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("wallettxuser")
                .displayname("Wallet Tx User")
                .email("wallettx@example.com")
                .build();
        entityManager.persist(user);

        wallet = WalletEntity.builder()
                .user(user)
                .balance(new BigDecimal("500.00"))
                .build();
        entityManager.persist(wallet);

        txSuccess = WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .status(StatusTransaction.SUCCESS)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        entityManager.persist(txSuccess);

        txPending = WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.DEPOSIT)
                .status(StatusTransaction.PENDING)
                .createdAt(Instant.now())
                .build();
        entityManager.persist(txPending);

        entityManager.flush();
    }

    @Test
    void findByWalletIdOrderByCreatedAtDesc_ShouldReturnTransactions() {
        List<WalletTransactionEntity> txs = walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        
        assertThat(txs).hasSize(2);
        assertThat(txs).extracting("id").contains(txSuccess.getId(), txPending.getId());
    }

    @Test
    void findByWalletUserId_ShouldReturnPagedTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WalletTransactionEntity> page = walletTransactionRepository.findByWalletUserId(user.getId(), pageable);
        
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findByWalletUserIdAndType_ShouldReturnTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WalletTransactionEntity> page = walletTransactionRepository.findByWalletUserIdAndType(user.getId(), TransactionType.DEPOSIT, pageable);
        
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findByTypeAndStatusOrderByCreatedAtDesc_ShouldReturnTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WalletTransactionEntity> page = walletTransactionRepository.findByTypeAndStatusOrderByCreatedAtDesc(TransactionType.DEPOSIT, StatusTransaction.SUCCESS, pageable);
        
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(txSuccess.getId());
    }

    @Test
    void findByTypeAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc_ShouldReturnTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Instant start = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant end = Instant.now();
        
        Page<WalletTransactionEntity> page = walletTransactionRepository.findByTypeAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                TransactionType.DEPOSIT, StatusTransaction.SUCCESS, start, end, pageable);
        
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(txSuccess.getId());
    }

    @Test
    void findRecentTransactions_ShouldReturnTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<WalletTransactionEntity> txs = walletTransactionRepository.findRecentTransactions(TransactionType.DEPOSIT, StatusTransaction.SUCCESS, pageable);
        
        assertThat(txs).isNotEmpty();
        assertThat(txs).extracting("id").contains(txSuccess.getId());
    }

    @Test
    void sumAmountByWalletIdAndTypeAndStatus_ShouldReturnSum() {
        BigDecimal sum = walletTransactionRepository.sumAmountByWalletIdAndTypeAndStatus(wallet.getId(), TransactionType.DEPOSIT, StatusTransaction.SUCCESS);
        
        assertThat(sum.compareTo(new BigDecimal("100.00"))).isEqualTo(0);
    }
}
