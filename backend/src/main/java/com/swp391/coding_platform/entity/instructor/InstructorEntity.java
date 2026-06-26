package com.swp391.coding_platform.entity.instructor;

import com.swp391.coding_platform.entity.enums.InstructorStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "instructors", schema = "public")
public class InstructorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Column(name = "full_name", nullable = false, length = 255)
    String fullName;

    @Column(name = "major", nullable = false, length = 255)
    String major;

    @Column(name = "bio", columnDefinition = "TEXT")
    String bio;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    InstructorStatus status = InstructorStatus.ACTIVE;

    @Builder.Default
    @Column(name = "hired_by_admin")
    Boolean hiredByAdmin = false;
}
