package com.aperture.scan.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record SuppressedIssueResponse(
        UUID id,
        UUID projectId,
        String fingerprint,
        String ruleCode,
        String filePath,
        String reason,
        LocalDateTime createdAt
) {
}
