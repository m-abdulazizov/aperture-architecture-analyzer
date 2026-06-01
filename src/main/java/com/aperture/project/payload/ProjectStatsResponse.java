package com.aperture.project.payload;

import com.aperture.scan.entity.IssueCategory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ProjectStatsResponse(
        UUID projectId,
        long scanCount,
        Integer latestScore,
        Integer previousScore,
        Integer scoreDelta,
        LocalDateTime latestScanAt,
        Map<IssueCategory, Long> latestIssuesByCategory
) {
}
