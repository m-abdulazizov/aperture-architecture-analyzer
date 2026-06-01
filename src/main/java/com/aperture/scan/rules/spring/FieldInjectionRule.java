package com.aperture.scan.rules.spring;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFieldContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FieldInjectionRule implements ScannerRule {

    @Override
    public String code() {
        return "SPRING_FIELD_INJECTION";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.SPRING;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.MEDIUM;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .flatMap(sourceFile -> sourceFile.fields().stream()
                        .filter(field -> field.annotations().contains("Autowired"))
                        .map(field -> issue(sourceFile, field)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, SourceFieldContext field) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Field injection is used",
                sourceFile.className() + "." + field.name() + " uses @Autowired field injection.",
                "Use constructor injection with final fields instead.",
                sourceFile.relativePath(),
                field.lineNumber()
        );
    }
}
