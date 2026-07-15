package com.swp391.coding_platform.entity.problem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

@Entity
@Table(name = "problem_visualizer_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemVisualizerCache {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "problem_id", nullable = false)
    private String problemId;

    @Column(name = "detected_algorithm", columnDefinition = "TEXT")
    private String detectedAlgorithm;

    @Column(name = "time_complexity", columnDefinition = "TEXT")
    private String timeComplexity;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(name = "prompt_version")
    private Integer promptVersion;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
