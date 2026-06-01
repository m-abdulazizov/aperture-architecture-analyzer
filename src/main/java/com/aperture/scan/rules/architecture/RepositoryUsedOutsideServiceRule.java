package com.aperture.scan.rules.architecture;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepositoryUsedOutsideServiceRule implements ScannerRule {

    @Override
    public String code() {
        return "ARCH_REPOSITORY_USED_OUTSIDE_SERVICE";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.ARCHITECTURE;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.MEDIUM;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .filter(sourceFile -> !sourceFile.controller() && !sourceFile.service() && !sourceFile.repository())
                .filter(this::usesRepository)
                .map(this::issue)
                .toList();
    }

    private boolean usesRepository(SourceFileContext sourceFile) {
        return sourceFile.fields().stream().anyMatch(field -> field.type().endsWith("Repository"))
                || sourceFile.imports().stream().anyMatch(importName -> importName.endsWith("Repository"));
    }

    private DetectedIssue issue(SourceFileContext sourceFile) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Repository used outside service layer",
                sourceFile.className() + " uses a repository outside the service layer.",
                "Keep repository access inside service classes and expose behavior through service methods.",
                sourceFile.relativePath(),
                1
        );
    }
}
