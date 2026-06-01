package com.aperture.scan.payload;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;

public record RuleMetadataResponse(
        String code,
        IssueCategory category,
        IssueSeverity defaultSeverity,
        String title,
        String description,
        String recommendation
) {
}
