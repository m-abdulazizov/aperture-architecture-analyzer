package com.aperture.scan.rules;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;

import java.util.List;

public interface ScannerRule {

    String code();

    IssueCategory category();

    IssueSeverity defaultSeverity();

    List<DetectedIssue> analyze(ProjectScanContext context);
}
