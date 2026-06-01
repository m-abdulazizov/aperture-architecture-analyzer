package com.aperture.scan.payload;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;

public record ScanIssueFilterRequest(
        IssueSeverity severity,
        IssueCategory category,
        String ruleCode
) {
}
