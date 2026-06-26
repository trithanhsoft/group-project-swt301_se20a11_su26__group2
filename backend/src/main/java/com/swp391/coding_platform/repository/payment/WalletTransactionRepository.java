package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Integer> {
    List<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(Integer walletId);
    Page<WalletTransactionEntity> findByWalletUserId(Integer userId, Pageable pageable);
    Page<WalletTransactionEntity> findByWalletUserIdAndType(Integer userId, TransactionType type, Pageable pageable);

    @Query("SELECT wt FROM WalletTransactionEntity wt " +
           "JOIN FETCH wt.wallet w " +
           "JOIN FETCH w.user u " +
           "WHERE wt.type = :type AND wt.status = :status " +
           "ORDER BY wt.createdAt DESC")
    List<WalletTransactionEntity> findRecentTransactions(
            @Param("type") TransactionType type,
            @Param("status") StatusTransaction status,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransactionEntity wt " +
           "WHERE wt.wallet.id = :walletId AND wt.type = :type AND wt.status = :status")
    BigDecimal sumAmountByWalletIdAndTypeAndStatus(
            @Param("walletId") Integer walletId,
            @Param("type") TransactionType type,
            @Param("status") StatusTransaction status);
}
