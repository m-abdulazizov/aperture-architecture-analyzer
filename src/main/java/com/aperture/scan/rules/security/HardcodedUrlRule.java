package com.aperture.scan.rules.security;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HardcodedUrlRule implements ScannerRule {

    @Override
    public String code() {
        return "SECURITY_HARDCODED_URL";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.SECURITY;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.LOW;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .flatMap(sourceFile -> sourceFile.compilationUnit().findAll(StringLiteralExpr.class).stream()
                        .filter(literal -> literal.asString().startsWith("http://") || literal.asString().startsWith("https://"))
                        .map(literal -> issue(sourceFile, literal)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, StringLiteralExpr literal) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Hardcoded URL",
                sourceFile.className() + " contains a hardcoded URL literal.",
                "Move environment-specific URLs into configuration.",
                sourceFile.relativePath(),
                literal.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
