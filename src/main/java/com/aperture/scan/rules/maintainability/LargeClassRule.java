package com.aperture.scan.rules.maintainability;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LargeClassRule implements ScannerRule {

    private static final int MAX_LINES = 300;

    @Override
    public String code() {
        return "MAINT_LARGE_CLASS";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.MAINTAINABILITY;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.LOW;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .filter(sourceFile -> sourceFile.lineCount() > MAX_LINES)
                .map(this::issue)
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Class is large",
                sourceFile.className() + " has " + sourceFile.lineCount() + " lines.",
                "Split large classes by responsibility and move behavior into focused collaborators.",
                sourceFile.relativePath(),
                1
        );
    }
}
