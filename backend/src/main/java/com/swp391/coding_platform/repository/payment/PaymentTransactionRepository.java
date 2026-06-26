package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import com.swp391.coding_platform.entity.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Integer> {
    Optional<PaymentTransactionEntity> findByTransactionCode(String transactionCode);
    Page<PaymentTransactionEntity> findByWalletUserId(Integer userId, Pageable pageable);
    Page<PaymentTransactionEntity> findByWalletUserIdAndType(Integer userId, PaymentType type, Pageable pageable);
    java.util.List<PaymentTransactionEntity> findByStatusAndCreatedAtBefore(com.swp391.coding_platform.entity.enums.StatusTransaction status, java.time.Instant timeLimit);
}
