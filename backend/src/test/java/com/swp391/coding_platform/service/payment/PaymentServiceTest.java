package com.swp391.coding_platform.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swp391.coding_platform.configuration.ProjectProperties;
import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.payment.PaymentTransactionRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.PayOS;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PayOS payOS;

    @Mock
    private ProjectProperties.Payos payosProps;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UserEntity user;
    private WalletEntity wallet;
    private PaymentTransactionEntity transaction;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1);
        user.setUsername("testuser");

        wallet = new WalletEntity();
        wallet.setId(1);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("100.00"));

        transaction = new PaymentTransactionEntity();
        transaction.setId(1);
        transaction.setWallet(wallet);
        transaction.setTransactionCode("123456");
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setType(PaymentType.DEPOSIT);
        transaction.setStatus(StatusTransaction.PENDING);
    }

    // ======================== getUserBalance ========================

    @Test
    void getUserBalance_UserHasWallet_ReturnsBalance() {
        when(walletRepository.findByUserId(1)).thenReturn(Optional.of(wallet));

        BigDecimal balance = paymentService.getUserBalance(1);

        assertEquals(new BigDecimal("100.00"), balance);
    }

    @Test
    void getUserBalance_UserHasNoWallet_ReturnsZero() {
        when(walletRepository.findByUserId(1)).thenReturn(Optional.empty());

        BigDecimal balance = paymentService.getUserBalance(1);

        assertEquals(BigDecimal.ZERO, balance);
    }

    // ======================== cancelPayment ========================

    @Test
    void cancelPayment_ValidRequest_CancelsPayment() throws Exception {
        when(paymentTransactionRepository.findByTransactionCode("123456")).thenReturn(Optional.of(transaction));

        paymentService.cancelPayment(1, "123456");

        assertEquals(StatusTransaction.CANCELLED, transaction.getStatus());
        verify(paymentTransactionRepository, times(1)).save(transaction);
        verify(payOS, times(1)).cancelPaymentLink(123456L, "Customer cancelled");
    }

    @Test
    void cancelPayment_TransactionNotFound_ThrowsException() {
        when(paymentTransactionRepository.findByTransactionCode("123456")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> paymentService.cancelPayment(1, "123456"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void cancelPayment_UserNotOwner_ThrowsException() {
        when(paymentTransactionRepository.findByTransactionCode("123456")).thenReturn(Optional.of(transaction));

        AppException ex = assertThrows(AppException.class, () -> paymentService.cancelPayment(2, "123456"));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void cancelPayment_TransactionAlreadyCancelled_ShouldReturnEarly() {
        // Transaction is already CANCELLED → the method should return early without calling PayOS
        transaction.setStatus(StatusTransaction.CANCELLED);
        when(paymentTransactionRepository.findByTransactionCode("123456")).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> paymentService.cancelPayment(1, "123456"));
        // Should not attempt to save or call PayOS again
        verify(paymentTransactionRepository, never()).save(any());
    }

    @Test
    void cancelPayment_TransactionAlreadySuccess_ShouldReturnEarly() {
        transaction.setStatus(StatusTransaction.SUCCESS);
        when(paymentTransactionRepository.findByTransactionCode("123456")).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> paymentService.cancelPayment(1, "123456"));
        verify(paymentTransactionRepository, never()).save(any());
    }

    // ======================== reconcilePendingTransactions ========================

    @Test
    void reconcilePendingTransactions_PaidTransaction_ProcessesPayment() throws Exception {
        when(paymentTransactionRepository.findByStatusAndCreatedAtBefore(eq(StatusTransaction.PENDING), any(Instant.class)))
                .thenReturn(Collections.singletonList(transaction));

        PaymentLinkData payosData = mock(PaymentLinkData.class);
        when(payosData.getStatus()).thenReturn("PAID");
        when(payOS.getPaymentLinkInformation(123456L)).thenReturn(payosData);

        when(walletRepository.findByUserIdWithLock(1)).thenReturn(Optional.of(wallet));

        paymentService.reconcilePendingTransactions();

        assertEquals(StatusTransaction.SUCCESS, transaction.getStatus());
        verify(paymentTransactionRepository, times(1)).save(transaction);
        verify(walletTransactionRepository, times(1)).save(any());
        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void reconcilePendingTransactions_CancelledTransaction_ShouldUpdateStatusToCancelled() throws Exception {
        when(paymentTransactionRepository.findByStatusAndCreatedAtBefore(eq(StatusTransaction.PENDING), any(Instant.class)))
                .thenReturn(Collections.singletonList(transaction));

        PaymentLinkData payosData = mock(PaymentLinkData.class);
        when(payosData.getStatus()).thenReturn("CANCELLED");
        when(payOS.getPaymentLinkInformation(123456L)).thenReturn(payosData);

        paymentService.reconcilePendingTransactions();

        assertEquals(StatusTransaction.CANCELLED, transaction.getStatus());
        verify(paymentTransactionRepository, times(1)).save(transaction);
        verify(walletTransactionRepository, never()).save(any()); // no wallet credit for cancelled
    }

    @Test
    void reconcilePendingTransactions_ExpiredTransaction_ShouldUpdateStatusToCancelled() throws Exception {
        when(paymentTransactionRepository.findByStatusAndCreatedAtBefore(eq(StatusTransaction.PENDING), any(Instant.class)))
                .thenReturn(Collections.singletonList(transaction));

        PaymentLinkData payosData = mock(PaymentLinkData.class);
        when(payosData.getStatus()).thenReturn("EXPIRED");
        when(payOS.getPaymentLinkInformation(123456L)).thenReturn(payosData);

        paymentService.reconcilePendingTransactions();

        assertEquals(StatusTransaction.CANCELLED, transaction.getStatus());
        verify(paymentTransactionRepository, times(1)).save(transaction);
    }

    @Test
    void reconcilePendingTransactions_NoPendingTransactions_ShouldDoNothing() {
        when(paymentTransactionRepository.findByStatusAndCreatedAtBefore(eq(StatusTransaction.PENDING), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> paymentService.reconcilePendingTransactions());
        verifyNoInteractions(payOS);
    }

    @Test
    void reconcilePendingTransactions_ExceptionPerTransaction_ShouldContinueProcessing() throws Exception {
        // Two transactions, first throws exception, second is normal
        PaymentTransactionEntity tx2 = new PaymentTransactionEntity();
        tx2.setId(2);
        tx2.setWallet(wallet);
        tx2.setTransactionCode("654321");
        tx2.setAmount(new BigDecimal("20.00"));
        tx2.setType(PaymentType.DEPOSIT);
        tx2.setStatus(StatusTransaction.PENDING);

        when(paymentTransactionRepository.findByStatusAndCreatedAtBefore(eq(StatusTransaction.PENDING), any(Instant.class)))
                .thenReturn(List.of(transaction, tx2));

        // First transaction throws
        when(payOS.getPaymentLinkInformation(123456L)).thenThrow(new RuntimeException("PayOS error"));
        // Second transaction succeeds
        PaymentLinkData payosData = mock(PaymentLinkData.class);
        when(payosData.getStatus()).thenReturn("PAID");
        when(payOS.getPaymentLinkInformation(654321L)).thenReturn(payosData);
        when(walletRepository.findByUserIdWithLock(1)).thenReturn(Optional.of(wallet));

        // Should not throw despite exception on first tx
        assertDoesNotThrow(() -> paymentService.reconcilePendingTransactions());

        // Second transaction should still be processed
        assertEquals(StatusTransaction.SUCCESS, tx2.getStatus());
    }

    // ======================== handlePayOSWebhook ========================

    @Test
    void handlePayOSWebhook_InvalidSignature_ThrowsException() throws Exception {
        ObjectNode payload = new ObjectMapper().createObjectNode();
        lenient().when(payOS.verifyPaymentWebhookData(any(Webhook.class))).thenThrow(new RuntimeException("Signature invalid"));

        AppException ex = assertThrows(AppException.class, () -> paymentService.handlePayOSWebhook(payload));
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, ex.getErrorCode());
    }

    @Test
    void handlePayOSWebhook_TransactionNotFound_ThrowsException() throws Exception {
        ObjectNode payload = new ObjectMapper().createObjectNode();
        WebhookData data = mock(WebhookData.class);
        lenient().when(data.getCode()).thenReturn("00");
        lenient().when(data.getOrderCode()).thenReturn(123456L);

        lenient().when(payOS.verifyPaymentWebhookData(any(Webhook.class))).thenReturn(data);
        lenient().when(paymentTransactionRepository.findByTransactionCode(anyString())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> paymentService.handlePayOSWebhook(payload));
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, ex.getErrorCode());
    }
}
