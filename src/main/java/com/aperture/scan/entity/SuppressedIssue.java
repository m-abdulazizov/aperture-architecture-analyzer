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
        name = "suppressed_issues",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_suppressed_issues_project_fingerprint", columnNames = {"project_id", "fingerprint"})
        },
        indexes = {
                @Index(name = "idx_suppressed_issues_project_id", columnList = "project_id"),
                @Index(name = "idx_suppressed_issues_fingerprint", columnList = "fingerprint"),
                @Index(name = "idx_suppressed_issues_rule_code", columnList = "rule_code")
        }
)
public class SuppressedIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 128)
    private String fingerprint;

    @Column(name = "rule_code", nullable = false, length = 150)
    private String ruleCode;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
