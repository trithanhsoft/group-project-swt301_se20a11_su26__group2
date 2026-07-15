package com.swp391.coding_platform.service.payment;

import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.event.UserRegisteredEvent;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void handleUserRegisteredEvent_HappyPath() {
        UserEntity user = new UserEntity();
        user.setId(1);
        UserRegisteredEvent event = mock(UserRegisteredEvent.class);
        when(event.getUserEntity()).thenReturn(user);

        walletService.handleUserRegisteredEvent(event);

        verify(walletRepository).save(any(WalletEntity.class));
    }
    
    @Test
    void handleUserRegisteredEvent_ExceptionThrown() {
        UserEntity user = new UserEntity();
        user.setId(1);
        UserRegisteredEvent event = mock(UserRegisteredEvent.class);
        when(event.getUserEntity()).thenReturn(user);

        when(walletRepository.save(any(WalletEntity.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> walletService.handleUserRegisteredEvent(event));
    }
}
