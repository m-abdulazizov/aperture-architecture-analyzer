package com.aperture.scan.rules.maintainability;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmptyCatchBlockRule implements ScannerRule {

    @Override
    public String code() {
        return "MAINT_EMPTY_CATCH_BLOCK";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.MAINTAINABILITY;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.MEDIUM;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .flatMap(sourceFile -> sourceFile.compilationUnit().findAll(CatchClause.class).stream()
                        .filter(catchClause -> catchClause.getBody().getStatements().isEmpty())
                        .map(catchClause -> issue(sourceFile, catchClause)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, CatchClause catchClause) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Catch block is empty",
                sourceFile.className() + " swallows an exception without handling or logging it.",
                "Handle the exception, log it, or explain why ignoring it is safe.",
                sourceFile.relativePath(),
                catchClause.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
