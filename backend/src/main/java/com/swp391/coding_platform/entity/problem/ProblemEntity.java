package com.swp391.coding_platform.entity.problem;

import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "problems", schema = "public")
public class ProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    UserEntity createdBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt = Instant.now();

    @Builder.Default
    @Column(name = "total_testcase", nullable = false)
    Integer totalTestcase = 0;

    @Builder.Default
    @Column(name = "total_submission")
    Integer totalSubmission = 0;

    @Builder.Default
    @Column(name = "total_accepted")
    Integer totalAccepted = 0;

    @Builder.Default
    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    BigDecimal score = new BigDecimal("100.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_scope")
    ProblemScope problemScope;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    Boolean isPublic = false;

    @Builder.Default
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProblemVersionEntity> versions = new ArrayList<>();

    public ProblemVersionEntity getCurrentVersion() {
        if (versions == null) return null;
        return versions.stream()
                .filter(ProblemVersionEntity::getIsActive)
                .findFirst()
                .orElse(null);
    }

    public void setCurrentVersion(ProblemVersionEntity version) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        for (ProblemVersionEntity v : this.versions) {
            v.setIsActive(false);
        }
        if (version != null) {
            version.setIsActive(true);
            if (!this.versions.contains(version)) {
                this.versions.add(version);
            }
        }
    }
}
