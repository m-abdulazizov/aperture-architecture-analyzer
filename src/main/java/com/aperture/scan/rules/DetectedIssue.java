package com.aperture.scan.rules;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;

public record DetectedIssue(
        IssueCategory category,
        IssueSeverity severity,
        String ruleCode,
        String title,
        String description,
        String recommendation,
        String filePath,
        Integer lineNumber
) {
}
