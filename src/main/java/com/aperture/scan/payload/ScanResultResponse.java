package com.aperture.scan.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanResultResponse(
        UUID id,
        UUID projectId,
        int totalScore,
        int architectureScore,
        int securityScore,
        int persistenceScore,
        int maintainabilityScore,
        int testingScore,
        int totalIssues,
        int criticalIssues,
        int highIssues,
        int mediumIssues,
        int lowIssues,
        int infoIssues,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
