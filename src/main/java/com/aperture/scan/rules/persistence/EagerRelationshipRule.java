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
public class EagerRelationshipRule implements ScannerRule {

    @Override
    public String code() {
        return "JPA_EAGER_RELATIONSHIP";
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
                        .filter(this::isRelationship)
                        .filter(field -> field.toString().contains("FetchType.EAGER"))
                        .map(field -> issue(entity, field)))
                .toList();
    }

    private boolean isRelationship(FieldDeclaration field) {
        return field.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .anyMatch(annotationName -> annotationName.equals("OneToOne")
                        || annotationName.equals("OneToMany")
                        || annotationName.equals("ManyToOne")
                        || annotationName.equals("ManyToMany"));
    }

    private DetectedIssue issue(SourceFileContext entity, FieldDeclaration field) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "JPA relationship uses FetchType.EAGER",
                entity.className() + " defines an eager relationship that can create unnecessary database loading.",
                "Use lazy loading unless eager fetching is intentionally required and documented.",
                entity.relativePath(),
                field.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
