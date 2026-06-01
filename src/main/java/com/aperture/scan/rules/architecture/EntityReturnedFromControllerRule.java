package com.aperture.scan.rules.architecture;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityReturnedFromControllerRule implements ScannerRule {

    @Override
    public String code() {
        return "ARCH_ENTITY_RETURNED_FROM_CONTROLLER";
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
        return context.controllers().stream()
                .flatMap(controller -> controller.compilationUnit().findAll(MethodDeclaration.class).stream()
                        .filter(method -> returnsEntity(context, method))
                        .map(method -> issue(controller, method)))
                .toList();
    }

    private boolean returnsEntity(ProjectScanContext context, MethodDeclaration method) {
        String returnType = method.getTypeAsString();
        return context.entities().stream()
                .anyMatch(entity -> returnType.equals(entity.className())
                        || returnType.contains("<" + entity.className() + ">")
                        || returnType.endsWith("<" + entity.className() + ">>")
                        || returnType.equals("ResponseEntity<" + entity.className() + ">"));
    }

    private DetectedIssue issue(SourceFileContext controller, MethodDeclaration method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Controller returns entity type",
                controller.className() + "." + method.getNameAsString() + " returns a persistence entity directly.",
                "Return a response DTO from controller methods instead of exposing JPA entities.",
                controller.relativePath(),
                method.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
