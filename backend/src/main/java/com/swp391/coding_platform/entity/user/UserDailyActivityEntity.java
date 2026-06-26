package com.swp391.coding_platform.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_daily_activities", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "activity_date"})
})
public class UserDailyActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Column(name = "activity_date", nullable = false)
    LocalDate activityDate;

    @Builder.Default
    @Column(name = "streak", nullable = false, columnDefinition = "integer default 1")
    Integer streak = 1;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();
}
