package com.aperture.scan.rules.spring;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.Parameter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestBodyWithoutValidRule implements ScannerRule {

    @Override
    public String code() {
        return "SPRING_REQUEST_BODY_WITHOUT_VALID";
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
        return context.controllers().stream()
                .flatMap(controller -> controller.compilationUnit().findAll(Parameter.class).stream()
                        .filter(this::hasRequestBody)
                        .filter(parameter -> !hasValidation(parameter))
                        .map(parameter -> issue(controller, parameter)))
                .toList();
    }

    private boolean hasRequestBody(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getName().getIdentifier().equals("RequestBody"));
    }

    private boolean hasValidation(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .map(annotation -> annotation.getName().getIdentifier())
                .anyMatch(annotationName -> annotationName.equals("Valid") || annotationName.equals("Validated"));
    }

    private DetectedIssue issue(SourceFileContext controller, Parameter parameter) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "@RequestBody parameter is not validated",
                parameter.getNameAsString() + " is annotated with @RequestBody but not @Valid or @Validated.",
                "Add @Valid or @Validated to request body parameters that accept DTOs.",
                controller.relativePath(),
                parameter.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
