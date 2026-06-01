package com.aperture.scan.payload;

import com.aperture.scan.entity.ScanJobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanJobResponse(
        UUID id,
        UUID projectId,
        UUID scanResultId,
        ScanJobStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
