package com.aperture.scan.rules.maintainability;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComplexMethodRule implements ScannerRule {

    private static final int MAX_COMPLEXITY = 10;

    @Override
    public String code() {
        return "MAINT_COMPLEX_METHOD";
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
                .flatMap(sourceFile -> sourceFile.compilationUnit().findAll(MethodDeclaration.class).stream()
                        .filter(method -> complexity(method) > MAX_COMPLEXITY)
                        .map(method -> issue(sourceFile, method, complexity(method))))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext sourceFile, MethodDeclaration method, int complexity) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Method has high cyclomatic complexity",
                sourceFile.className() + "." + method.getNameAsString()
                        + " has an estimated cyclomatic complexity of " + complexity + ".",
                "Split branches into smaller methods, use guard clauses, or move decision logic into focused collaborators.",
                sourceFile.relativePath(),
                method.getRange().map(range -> range.begin.line).orElse(null)
        );
    }

    private int complexity(MethodDeclaration method) {
        return 1
                + method.findAll(IfStmt.class).size()
                + method.findAll(ForStmt.class).size()
                + method.findAll(ForEachStmt.class).size()
                + method.findAll(WhileStmt.class).size()
                + method.findAll(DoStmt.class).size()
                + method.findAll(CatchClause.class).size()
                + method.findAll(ConditionalExpr.class).size()
                + method.findAll(SwitchEntry.class).size();
    }
}
