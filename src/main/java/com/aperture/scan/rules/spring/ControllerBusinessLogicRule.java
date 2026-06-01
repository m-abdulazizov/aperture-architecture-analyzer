package com.aperture.scan.rules.spring;

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
public class ControllerBusinessLogicRule implements ScannerRule {

    private static final int MAX_CONTROLLER_METHOD_LINES = 25;

    @Override
    public String code() {
        return "SPRING_CONTROLLER_BUSINESS_LOGIC";
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
                .flatMap(controller -> controller.methods().stream()
                        .filter(method -> method.lineCount() > MAX_CONTROLLER_METHOD_LINES)
                        .map(method -> issue(controller, method)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext controller, SourceMethodContext method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Controller method may contain business logic",
                controller.className() + "." + method.name() + " has " + method.lineCount() + " lines.",
                "Keep controllers thin and move business decisions into services.",
                controller.relativePath(),
                method.lineNumber()
        );
    }
}
