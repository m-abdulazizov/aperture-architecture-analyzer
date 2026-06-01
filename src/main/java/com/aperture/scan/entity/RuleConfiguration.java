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
        name = "rule_configurations",
        uniqueConstraints = @UniqueConstraint(name = "uq_rule_config_project_rule", columnNames = {"project_id", "rule_code"}),
        indexes = @Index(name = "idx_rule_config_rule_code", columnList = "rule_code")
)
public class RuleConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "rule_code", nullable = false, length = 150)
    private String ruleCode;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_override", length = 50)
    private IssueSeverity severityOverride;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
