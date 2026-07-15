package com.swp391.coding_platform.service.payment;

import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.mapper.WalletTransactionMapper;
import com.swp391.coding_platform.repository.payment.PaymentTransactionRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.mapper.PaymentTransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionStatisticsServiceTest {

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private WalletTransactionMapper walletTransactionMapper;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @InjectMocks
    private TransactionStatisticsService transactionStatisticsService;

    @Test
    void getTransactionStatistics_HappyPath() {
        Integer userId = 1;
        TransactionType type = TransactionType.BUY_COURSE;
        Pageable pageable = PageRequest.of(0, 10);
        
        WalletTransactionEntity entity = new WalletTransactionEntity();
        Page<WalletTransactionEntity> page = new PageImpl<>(List.of(entity));
        
        when(walletTransactionRepository.findByWalletUserIdAndType(eq(userId), eq(type), any(Pageable.class))).thenReturn(page);
        
        TransactionStatisticResponse responseDto = new TransactionStatisticResponse();
        when(walletTransactionMapper.toTransactionStatisticResponse(any())).thenReturn(responseDto);

        PageResponse<TransactionStatisticResponse> result = transactionStatisticsService.getTransactionStatistics(userId, type, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactionStatistics_NullType_ReturnsAll() {
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        
        WalletTransactionEntity entity = new WalletTransactionEntity();
        Page<WalletTransactionEntity> page = new PageImpl<>(List.of(entity));
        
        when(walletTransactionRepository.findByWalletUserId(eq(userId), any(Pageable.class))).thenReturn(page);
        
        TransactionStatisticResponse responseDto = new TransactionStatisticResponse();
        when(walletTransactionMapper.toTransactionStatisticResponse(any())).thenReturn(responseDto);

        PageResponse<TransactionStatisticResponse> result = transactionStatisticsService.getTransactionStatistics(userId, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getTransactionStatistics_DatabaseError_ThrowsException() {
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        when(walletTransactionRepository.findByWalletUserId(eq(userId), any(Pageable.class)))
                .thenThrow(new RuntimeException("DB Connection Error"));

        assertThrows(RuntimeException.class, () -> transactionStatisticsService.getTransactionStatistics(userId, null, pageable));
    }
}
