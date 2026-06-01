package com.aperture.scan.service;

import com.aperture.common.exception.ProjectNotFoundException;
import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.repository.ProjectRepository;
import com.aperture.scan.entity.ScanJob;
import com.aperture.scan.payload.ScanJobResponse;
import com.aperture.scan.repository.ScanJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScanJobService {

    private final ProjectRepository projectRepository;
    private final ScanJobRepository scanJobRepository;
    private final AsyncScanWorker asyncScanWorker;

    @Transactional
    public ScanJobResponse startAsyncScan(UUID projectId) {
        Project project = projectRepository.findByIdAndStatusNot(projectId, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (project.getExtractedPath() == null || project.getExtractedPath().isBlank()) {
            throw new ScanFailedException("Project archive must be uploaded before scanning");
        }

        ScanJob job = scanJobRepository.save(ScanJob.builder()
                .project(project)
                .build());

        dispatchAfterCommit(job.getId(), project.getId());

        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public ScanJobResponse getScanJob(UUID scanJobId) {
        return scanJobRepository.findById(scanJobId)
                .map(this::toResponse)
                .orElseThrow(() -> new ScanFailedException("Scan job not found with id: " + scanJobId));
    }

    @Transactional(readOnly = true)
    public Page<ScanJobResponse> getProjectScanJobs(UUID projectId, Pageable pageable) {
        return scanJobRepository.findAllByProjectId(projectId, pageable)
                .map(this::toResponse);
    }

    private ScanJobResponse toResponse(ScanJob job) {
        return new ScanJobResponse(
                job.getId(),
                job.getProject().getId(),
                job.getScanResult() == null ? null : job.getScanResult().getId(),
                job.getStatus(),
                job.getStage(),
                job.getProgressPercent(),
                job.getFailureReason(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }

    private void dispatchAfterCommit(UUID scanJobId, UUID projectId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncScanWorker.run(scanJobId, projectId);
            }
        });
    }
}
