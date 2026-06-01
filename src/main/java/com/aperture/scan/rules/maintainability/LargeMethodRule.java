package com.aperture.scan.rules.maintainability;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.engine.SourceMethodContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LargeMethodRule implements ScannerRule {

    private static final int MAX_LINES = 60;

    @Override
    public String code() {
        return "MAINT_LARGE_METHOD";
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
                .flatMap(sourceFile -> sourceFile.methods().stream()
                        .filter(method -> method.lineCount() > MAX_LINES)
                        .map(method -> issue(sourceFile, method)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, SourceMethodContext method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Method is large",
                sourceFile.className() + "." + method.name() + " has " + method.lineCount() + " lines.",
                "Extract smaller private methods or move cohesive behavior into collaborators.",
                sourceFile.relativePath(),
                method.lineNumber()
        );
    }
}
