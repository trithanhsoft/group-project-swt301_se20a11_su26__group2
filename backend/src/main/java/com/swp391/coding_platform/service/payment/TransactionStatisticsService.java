package com.swp391.coding_platform.service.payment;

import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.mapper.WalletTransactionMapper;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.dto.response.PaymentTransactionStatisticResponse;
import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import com.swp391.coding_platform.mapper.PaymentTransactionMapper;
import com.swp391.coding_platform.repository.payment.PaymentTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionStatisticsService {

    WalletTransactionRepository walletTransactionRepository;
    WalletTransactionMapper walletTransactionMapper;
    PaymentTransactionRepository paymentTransactionRepository;
    PaymentTransactionMapper paymentTransactionMapper;

    @Transactional(readOnly = true)
    public PageResponse<TransactionStatisticResponse> getTransactionStatistics(Integer userId, TransactionType type, Pageable pageable) {
        // Enforce maximum of 10 records per page as requested
        int pageSize = Math.min(pageable.getPageSize(), 10);

        // Always sort by transaction time descending (newest transactions first)
        Pageable cappedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<WalletTransactionEntity> transactionPage;
        if (type == null) {
            transactionPage = walletTransactionRepository.findByWalletUserId(userId, cappedPageable);
        } else {
            transactionPage = walletTransactionRepository.findByWalletUserIdAndType(userId, type, cappedPageable);
        }

        // Map using MapStruct mapper
        Page<TransactionStatisticResponse> dtoPage = transactionPage.map(walletTransactionMapper::toTransactionStatisticResponse);

        return PageResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentTransactionStatisticResponse> getPaymentTransactionStatistics(Integer userId, PaymentType type, Pageable pageable) {
        // Enforce maximum of 10 records per page as requested
        int pageSize = Math.min(pageable.getPageSize(), 10);

        // Always sort by transaction time descending (newest transactions first)
        Pageable cappedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<PaymentTransactionEntity> transactionPage;
        if (type == null) {
            transactionPage = paymentTransactionRepository.findByWalletUserId(userId, cappedPageable);
        } else {
            transactionPage = paymentTransactionRepository.findByWalletUserIdAndType(userId, type, cappedPageable);
        }

        // Map using MapStruct mapper
        Page<PaymentTransactionStatisticResponse> dtoPage = transactionPage.map(paymentTransactionMapper::toPaymentTransactionStatisticResponse);

        return PageResponse.from(dtoPage);
    }
}
