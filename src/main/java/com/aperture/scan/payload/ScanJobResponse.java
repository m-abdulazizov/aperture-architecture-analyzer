package com.aperture.scan.payload;

import com.aperture.scan.entity.ScanJobStatus;
import com.aperture.scan.entity.ScanJobStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanJobResponse(
        UUID id,
        UUID projectId,
        UUID scanResultId,
        ScanJobStatus status,
        ScanJobStage stage,
        int progressPercent,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
