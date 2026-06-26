package com.swp391.coding_platform.entity.payment;

import com.swp391.coding_platform.entity.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payout_requests", schema = "public")
public class PayoutRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    WalletEntity wallet;

    @Column(name = "payout_period", nullable = false, length = 50)
    String payoutPeriod;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Column(name = "bank_name", nullable = false, length = 255)
    String bankName;

    @Column(name = "bank_account_number", nullable = false, length = 255)
    String bankAccountNumber;

    @Column(name = "bank_account_name", nullable = false, length = 255)
    String bankAccountName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "status")
    PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "transaction_reference", length = 255)
    String transactionReference;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    String adminNote;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();
}
