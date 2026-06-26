package com.swp391.coding_platform.service.payment;

import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.event.UserRegisteredEvent;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {
    WalletRepository walletRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        UserEntity user = event.getUserEntity();

        // Logic tạo ví
        WalletEntity wallet = WalletEntity.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .status(UserStatus.ACTIVE)
                .build();

        walletRepository.save(wallet);
    }

}

