package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.payment.PayoutRequestEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PayoutRequestRepositoryTest {

    @Autowired
    private PayoutRequestRepository payoutRequestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private WalletEntity wallet;
    private PayoutRequestEntity request1;
    private PayoutRequestEntity request2;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("payoutuser")
                .displayname("Payout User")
                .email("payout@example.com")
                .build();
        entityManager.persist(user);

        wallet = WalletEntity.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .build();
        entityManager.persist(wallet);

        request1 = PayoutRequestEntity.builder()
                .wallet(wallet)
                .payoutPeriod("2023-01")
                .amount(new BigDecimal("100.00"))
                .bankName("Bank A")
                .bankAccountNumber("123456")
                .bankAccountName("John Doe")
                .build();
        entityManager.persist(request1);

        request2 = PayoutRequestEntity.builder()
                .wallet(wallet)
                .payoutPeriod("2023-02")
                .amount(new BigDecimal("200.00"))
                .bankName("Bank B")
                .bankAccountNumber("654321")
                .bankAccountName("John Doe")
                .build();
        entityManager.persist(request2);

        entityManager.flush();
    }

    @Test
    void findByWalletIdOrderByCreatedAtDesc_ShouldReturnRequests() {
        List<PayoutRequestEntity> requests = payoutRequestRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting("id").containsExactlyInAnyOrder(request1.getId(), request2.getId());
    }
}
