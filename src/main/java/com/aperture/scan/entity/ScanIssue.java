package com.aperture.scan.entity;

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
        name = "scan_issues",
        indexes = {
                @Index(name = "idx_scan_issues_scan_result_id", columnList = "scan_result_id"),
                @Index(name = "idx_scan_issues_category", columnList = "category"),
                @Index(name = "idx_scan_issues_severity", columnList = "severity"),
                @Index(name = "idx_scan_issues_rule_code", columnList = "rule_code"),
                @Index(name = "idx_scan_issues_fingerprint", columnList = "fingerprint")
        }
)
public class ScanIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_result_id", nullable = false)
    private ScanResult scanResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssueSeverity severity;

    @Column(name = "rule_code", nullable = false, length = 150)
    private String ruleCode;

    @Column(nullable = false, length = 128)
    private String fingerprint;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String recommendation;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
