package com.aperture.scan.rules.maintainability;

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
public class TooManyParametersRule implements ScannerRule {

    private static final int MAX_PARAMETERS = 5;

    @Override
    public String code() {
        return "MAINT_TOO_MANY_PARAMETERS";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.MAINTAINABILITY;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.LOW;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .flatMap(sourceFile -> sourceFile.methods().stream()
                        .filter(method -> method.parameters().size() > MAX_PARAMETERS)
                        .map(method -> issue(sourceFile, method)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, SourceMethodContext method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Method has too many parameters",
                sourceFile.className() + "." + method.name()
                        + " has " + method.parameters().size() + " parameters.",
                "Group related inputs into a request object or split responsibilities across smaller methods.",
                sourceFile.relativePath(),
                method.lineNumber()
        );
    }
}
