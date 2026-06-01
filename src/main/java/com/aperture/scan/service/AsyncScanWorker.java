package com.aperture.scan.service;

import com.aperture.scan.entity.ScanJob;
import com.aperture.scan.entity.ScanJobStage;
import com.aperture.scan.entity.ScanJobStatus;
import com.aperture.scan.payload.ScanResultResponse;
import com.aperture.scan.repository.ScanJobRepository;
import com.aperture.scan.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncScanWorker {

    private final ScanService scanService;
    private final ScanJobRepository scanJobRepository;
    private final ScanResultRepository scanResultRepository;

    @Async
    @Transactional
    public void run(UUID scanJobId, UUID projectId) {
        ScanJob job = scanJobRepository.findById(scanJobId).orElseThrow();
        job.setStatus(ScanJobStatus.RUNNING);
        job.setStage(ScanJobStage.DISCOVERING_FILES);
        job.setProgressPercent(10);
        job.setStartedAt(LocalDateTime.now());
        scanJobRepository.save(job);

        try {
            updateProgress(job, ScanJobStage.RUNNING_RULES, 45);
            ScanResultResponse scanResult = scanService.scanProject(projectId);
            updateProgress(job, ScanJobStage.SAVING_REPORT, 90);
            job.setScanResult(scanResultRepository.findById(scanResult.id()).orElseThrow());
            job.setStatus(ScanJobStatus.COMPLETED);
            job.setStage(ScanJobStage.COMPLETED);
            job.setProgressPercent(100);
            job.setFinishedAt(LocalDateTime.now());
            scanJobRepository.save(job);
        } catch (RuntimeException exception) {
            job.setStatus(ScanJobStatus.FAILED);
            job.setStage(ScanJobStage.FAILED);
            job.setFailureReason(exception.getMessage());
            job.setFinishedAt(LocalDateTime.now());
            scanJobRepository.save(job);
        }
    }

    private void updateProgress(ScanJob job, ScanJobStage stage, int progressPercent) {
        job.setStage(stage);
        job.setProgressPercent(progressPercent);
        scanJobRepository.save(job);
    }
}
