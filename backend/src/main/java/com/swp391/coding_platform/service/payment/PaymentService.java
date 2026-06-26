package com.swp391.coding_platform.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swp391.coding_platform.configuration.ProjectProperties;
import com.swp391.coding_platform.dto.request.PaymentDepositRequest;
import com.swp391.coding_platform.dto.response.PaymentDepositResponse;
import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.payment.PaymentTransactionRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;
import vn.payos.type.PaymentLinkData;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PayOS payOS;
    ProjectProperties.Payos payosProps;
    WalletRepository walletRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    WalletTransactionRepository walletTransactionRepository;
    UserRepository userRepository;

    @Transactional
    public PaymentDepositResponse createDepositPayment(Integer userId, PaymentDepositRequest request) {

        // 0. Fetch user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.validateStatus();

        // 1. Get Wallet (Create on the fly if missing)
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Wallet not found for user {}, creating a new one.", userId);
                    WalletEntity newWallet = WalletEntity.builder()
                            .user(user)
                            .balance(BigDecimal.ZERO)
                            .status(UserStatus.ACTIVE)
                            .build();
                    return walletRepository.save(newWallet);
                });

        // 2. Generate a unique transaction code (using current time ms for simplicity, PayOS requires integer orderCode)
        long orderCode = System.currentTimeMillis();
        String transactionCode = String.valueOf(orderCode);

        // 3. Save pending transaction
        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .wallet(wallet)
                .transactionCode(transactionCode)
                .amount(request.getAmount())
                .type(PaymentType.DEPOSIT)
                .status(StatusTransaction.PENDING)
                .build();
        paymentTransactionRepository.save(transaction);

        // 4. Create link with PayOS SDK
        try {
            // Manual call to bypass SDK ObjectMapper bug with new fields (expiredAt)
            Map<String, Object> body = new HashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", request.getAmount().intValue());
            body.put("description", "Thanh toan nap Xu");
            body.put("returnUrl", payosProps.getReturnUrl());
            body.put("cancelUrl", payosProps.getCancelUrl());
            
            // Create signature
            String signData = "amount=" + body.get("amount") +
                              "&cancelUrl=" + body.get("cancelUrl") +
                              "&description=" + body.get("description") +
                              "&orderCode=" + body.get("orderCode") +
                              "&returnUrl=" + body.get("returnUrl");
            String signature = generateHmacSHA256(signData, payosProps.getChecksumKey());
            body.put("signature", signature);

            // Create WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api-merchant.payos.vn/v2/payment-requests")
                    .defaultHeader("x-client-id", payosProps.getClientId())
                    .defaultHeader("x-api-key", payosProps.getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .build();

            // Execute POST request synchronously
            JsonNode responseNode = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // Block since the outer method is synchronous

            if (responseNode == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            
            if (!"00".equals(responseNode.path("code").asText())) {
                log.error("PayOS API returned error: {}", responseNode.toString());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            JsonNode dataNode = responseNode.get("data");
            if (dataNode == null || dataNode.isNull() || !dataNode.has("checkoutUrl")) {
                log.error("PayOS API response is missing data/checkoutUrl: {}", responseNode.toString());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            
            String checkoutUrl = dataNode.get("checkoutUrl").asText();
            String qrCode = dataNode.hasNonNull("qrCode") ? dataNode.get("qrCode").asText() : null;
            String accountNumber = dataNode.hasNonNull("accountNumber") ? dataNode.get("accountNumber").asText() : null;
            String accountName = dataNode.hasNonNull("accountName") ? dataNode.get("accountName").asText() : null;
            String bin = dataNode.hasNonNull("bin") ? dataNode.get("bin").asText() : null;

            return PaymentDepositResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .transactionCode(transactionCode)
                    .qrCode(qrCode)
                    .accountNumber(accountNumber)
                    .accountName(accountName)
                    .bin(bin)
                    .build();

        } catch (Exception e) {
            log.error("PayOS Error: ", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public void handlePayOSWebhook(ObjectNode payload) {
        try {
            // 1. Parse string to Webhook type
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhookBody = objectMapper.treeToValue(payload, Webhook.class);
            
            // 2. Verify signature (Throws exception if invalid)
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);

            if (!"00".equals(data.getCode())) {
                log.info("Webhook received but not a success code: {}", data.getCode());
                return;
            }

            // 3. Process the successful payment
            String transactionCode = String.valueOf(data.getOrderCode());
            PaymentTransactionEntity paymentTx = paymentTransactionRepository.findByTransactionCode(transactionCode)
                    .orElseThrow(() -> {
                        log.warn("Webhook received for unknown transaction: {}", transactionCode);
                        return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                    });

            // Idempotency check: if not PENDING, we already processed it
            if (paymentTx.getStatus() != StatusTransaction.PENDING) {
                log.info("Transaction {} already processed. Status: {}", transactionCode, paymentTx.getStatus());
                return;
            }

            // 4. Lock Wallet and Process
            processSuccessfulPayment(paymentTx);

        } catch (Exception e) {
            log.error("Error processing PayOS Webhook", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private String generateHmacSHA256(String data, String key) {
        try {
            javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
    }

    public BigDecimal getUserBalance(Integer userId) {
        return walletRepository.findByUserId(userId)
                .map(WalletEntity::getBalance)
                .orElse(java.math.BigDecimal.ZERO);
    }

    @Transactional
    public void cancelPayment(Integer userId, String transactionCode) {
        PaymentTransactionEntity transaction = paymentTransactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!transaction.getWallet().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (transaction.getStatus() != StatusTransaction.PENDING) {
            return;
        }

        transaction.setStatus(StatusTransaction.CANCELLED);
        paymentTransactionRepository.save(transaction);

        try {
            long orderCode = Long.parseLong(transactionCode);
            payOS.cancelPaymentLink(orderCode, "Customer cancelled");
        } catch (Exception e) {
            log.warn("Failed to cancel PayOS payment link for orderCode {}: {}", transactionCode, e.getMessage());
        }
    }

    private void processSuccessfulPayment(PaymentTransactionEntity paymentTx) {
        WalletEntity wallet = walletRepository.findByUserIdWithLock(paymentTx.getWallet().getUser().getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        paymentTx.setStatus(StatusTransaction.SUCCESS);
        paymentTransactionRepository.save(paymentTx);

        WalletTransactionEntity walletTx = WalletTransactionEntity.builder()
                .wallet(wallet)
                .amount(paymentTx.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(StatusTransaction.SUCCESS)
                .referenceId(paymentTx.getId().toString())
                .build();
        walletTransactionRepository.save(walletTx);

        wallet.setBalance(wallet.getBalance().add(paymentTx.getAmount()));
        walletRepository.save(wallet);

        log.info("Successfully processed deposit for transaction: {}", paymentTx.getTransactionCode());
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void reconcilePendingTransactions() {
        Instant timeLimit = Instant.now().minus(24, ChronoUnit.HOURS);
        List<PaymentTransactionEntity> pendingTxs = paymentTransactionRepository
                .findByStatusAndCreatedAtBefore(StatusTransaction.PENDING, timeLimit);

        for (PaymentTransactionEntity tx : pendingTxs) {
            try {
                long orderCode = Long.parseLong(tx.getTransactionCode());
                PaymentLinkData payosData = payOS.getPaymentLinkInformation(orderCode);

                if ("PAID".equals(payosData.getStatus())) {
                    processSuccessfulPayment(tx);
                    log.info("Reconciled and recovered missing webhook for transaction: {}", orderCode);
                } else if ("CANCELLED".equals(payosData.getStatus()) || "EXPIRED".equals(payosData.getStatus())) {
                    tx.setStatus(StatusTransaction.CANCELLED);
                    paymentTransactionRepository.save(tx);
                    log.info("Cleaned up abandoned transaction: {}", orderCode);
                } else if ("PENDING".equals(payosData.getStatus())) {
                    payOS.cancelPaymentLink(orderCode, "Expired after 24h");
                    tx.setStatus(StatusTransaction.CANCELLED);
                    paymentTransactionRepository.save(tx);
                    log.info("Cancelled expired pending transaction: {}", orderCode);
                }
            } catch (Exception e) {
                log.error("Error reconciling transaction: {}", tx.getTransactionCode(), e);
            }
        }
    }
}
