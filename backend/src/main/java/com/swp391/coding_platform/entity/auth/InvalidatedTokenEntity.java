package com.swp391.coding_platform.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invalidated_tokens", schema = "public")
public class InvalidatedTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "token", nullable = false, length = 255)
    String tokenJti;

    @Column(name = "expiry_time", nullable = false)
    OffsetDateTime expiryTime;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt = OffsetDateTime.now();
}
