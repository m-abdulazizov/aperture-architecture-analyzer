package com.aperture.scan.payload;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanIssueResponse(
        UUID id,
        IssueCategory category,
        IssueSeverity severity,
        String ruleCode,
        String title,
        String description,
        String recommendation,
        String filePath,
        Integer lineNumber,
        LocalDateTime createdAt
) {
}
