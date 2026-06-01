package com.aperture.scan.rules.testing;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MissingTestClassRule implements ScannerRule {

    @Override
    public String code() {
        return "TEST_MISSING_CLASS_TEST";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.TESTING;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.MEDIUM;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        Set<String> testClassNames = context.sourceFiles().stream()
                .filter(this::isTestSource)
                .map(SourceFileContext::className)
                .collect(Collectors.toSet());

        return context.sourceFiles().stream()
                .filter(this::isProductionSource)
                .filter(this::isImportantSpringType)
                .filter(sourceFile -> !hasMatchingTest(sourceFile, testClassNames))
                .map(this::issue)
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Production class has no matching test class",
                sourceFile.className() + " does not have a matching " + sourceFile.className() + "Test or "
                        + sourceFile.className() + "Tests class under src/test/java.",
                "Add focused unit or slice tests for the class behavior and keep the test name aligned with the class name.",
                sourceFile.relativePath(),
                1
        );
    }

    private boolean isProductionSource(SourceFileContext sourceFile) {
        return sourceFile.relativePath().startsWith("src/main/java/");
    }

    private boolean isTestSource(SourceFileContext sourceFile) {
        return sourceFile.relativePath().startsWith("src/test/java/");
    }

    private boolean isImportantSpringType(SourceFileContext sourceFile) {
        return sourceFile.controller()
                || sourceFile.service()
                || sourceFile.repository()
                || sourceFile.className().endsWith("Service")
                || sourceFile.className().endsWith("Controller")
                || sourceFile.className().endsWith("Repository");
    }

    private boolean hasMatchingTest(SourceFileContext sourceFile, Set<String> testClassNames) {
        return testClassNames.contains(sourceFile.className() + "Test")
                || testClassNames.contains(sourceFile.className() + "Tests");
    }
}
