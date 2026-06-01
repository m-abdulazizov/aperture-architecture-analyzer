package com.aperture.scan.rules.architecture;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFieldContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ControllerRepositoryDependencyRule implements ScannerRule {

    @Override
    public String code() {
        return "ARCH_CONTROLLER_REPOSITORY_DEPENDENCY";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.ARCHITECTURE;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.HIGH;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.controllers().stream()
                .flatMap(controller -> repositoryField(controller).stream()
                        .map(field -> issue(controller, field)))
                .toList();
    }

    private Optional<SourceFieldContext> repositoryField(SourceFileContext controller) {
        Optional<SourceFieldContext> fieldMatch = controller.fields().stream()
                .filter(field -> field.type().endsWith("Repository"))
                .findFirst();

        if (fieldMatch.isPresent()) {
            return fieldMatch;
        }

        return controller.imports().stream()
                .filter(importName -> importName.endsWith("Repository"))
                .findFirst()
                .map(importName -> new SourceFieldContext(importName.substring(importName.lastIndexOf('.') + 1), importName, List.of(), 1));
    }

    private DetectedIssue issue(SourceFileContext controller, SourceFieldContext field) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Controller directly depends on Repository",
                controller.className() + " directly depends on " + field.type() + ".",
                "Move repository access into a service class and inject the service into the controller.",
                controller.relativePath(),
                field.lineNumber()
        );
    }
}
