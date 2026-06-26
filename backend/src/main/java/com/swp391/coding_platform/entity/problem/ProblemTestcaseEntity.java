package com.swp391.coding_platform.entity.problem;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "problem_testcases", schema = "public")
public class ProblemTestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    ProblemEntity problem;

    @Column(name = "input_data", nullable = false, columnDefinition = "TEXT")
    String inputData;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    String expectedOutput;

    @Column(name = "order_index", nullable = false)
    Integer orderIndex;

    @Column(name = "token", length = 255)
    String token;
}
