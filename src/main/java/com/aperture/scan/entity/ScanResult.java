package com.aperture.scan.entity;

import com.aperture.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "scan_results",
        indexes = {
                @Index(name = "idx_scan_results_project_id", columnList = "project_id"),
                @Index(name = "idx_scan_results_created_at", columnList = "created_at")
        }
)
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "architecture_score", nullable = false)
    private int architectureScore;

    @Column(name = "security_score", nullable = false)
    private int securityScore;

    @Column(name = "persistence_score", nullable = false)
    private int persistenceScore;

    @Column(name = "maintainability_score", nullable = false)
    private int maintainabilityScore;

    @Column(name = "testing_score", nullable = false)
    private int testingScore;

    @Column(name = "total_issues", nullable = false)
    private int totalIssues;

    @Column(name = "critical_issues", nullable = false)
    private int criticalIssues;

    @Column(name = "high_issues", nullable = false)
    private int highIssues;

    @Column(name = "medium_issues", nullable = false)
    private int mediumIssues;

    @Column(name = "low_issues", nullable = false)
    private int lowIssues;

    @Column(name = "info_issues", nullable = false)
    private int infoIssues;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
