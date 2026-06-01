package com.aperture.scan.rules.spring;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionalOnControllerRule implements ScannerRule {

    @Override
    public String code() {
        return "SPRING_TRANSACTIONAL_ON_CONTROLLER";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.SPRING;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.HIGH;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        List<DetectedIssue> issues = new ArrayList<>();

        for (SourceFileContext controller : context.controllers()) {
            if (controller.annotations().contains("Transactional")) {
                issues.add(issue(controller, controller.className(), 1));
            }

            controller.compilationUnit().findAll(MethodDeclaration.class).stream()
                    .filter(method -> method.getAnnotations().stream()
                            .anyMatch(annotation -> annotation.getName().getIdentifier().equals("Transactional")))
                    .map(method -> issue(
                            controller,
                            controller.className() + "." + method.getNameAsString(),
                            method.getRange().map(range -> range.begin.line).orElse(null)
                    ))
                    .forEach(issues::add);
        }

        return issues;
    }

    private DetectedIssue issue(SourceFileContext controller, String location, Integer lineNumber) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "@Transactional used in controller",
                location + " defines a transaction boundary in the web layer.",
                "Move transaction boundaries to the service layer.",
                controller.relativePath(),
                lineNumber
        );
    }
}
