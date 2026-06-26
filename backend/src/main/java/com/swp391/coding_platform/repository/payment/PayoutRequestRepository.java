package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.payment.PayoutRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayoutRequestRepository extends JpaRepository<PayoutRequestEntity, Integer> {
    List<PayoutRequestEntity> findByWalletIdOrderByCreatedAtDesc(Integer walletId);
}
