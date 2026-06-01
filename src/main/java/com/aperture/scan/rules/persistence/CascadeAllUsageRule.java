package com.aperture.scan.rules.persistence;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CascadeAllUsageRule implements ScannerRule {

    @Override
    public String code() {
        return "JPA_CASCADE_ALL_USAGE";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.PERSISTENCE;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.MEDIUM;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.entities().stream()
                .flatMap(entity -> entity.compilationUnit().findAll(FieldDeclaration.class).stream()
                        .filter(field -> field.toString().contains("CascadeType.ALL"))
                        .map(field -> issue(entity, field)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext entity, FieldDeclaration field) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "JPA relationship uses CascadeType.ALL",
                entity.className() + " uses CascadeType.ALL, which can propagate deletes and updates too broadly.",
                "Use the narrowest cascade types needed by the relationship.",
                entity.relativePath(),
                field.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
