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
        name = "scan_jobs",
        indexes = {
                @Index(name = "idx_scan_jobs_project_id", columnList = "project_id"),
                @Index(name = "idx_scan_jobs_status", columnList = "status"),
                @Index(name = "idx_scan_jobs_created_at", columnList = "created_at")
        }
)
public class ScanJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_result_id")
    private ScanResult scanResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScanJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ScanJobStage stage;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ScanJobStatus.QUEUED;
        }
        if (this.stage == null) {
            this.stage = ScanJobStage.QUEUED;
        }
    }
}
