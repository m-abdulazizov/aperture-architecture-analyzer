package com.aperture.scan.rules.spring;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class MissingTransactionalOnWriteServiceRule implements ScannerRule {

    private static final Set<String> WRITE_PREFIXES = Set.of("create", "update", "delete", "save", "remove");

    @Override
    public String code() {
        return "SPRING_SERVICE_WRITE_WITHOUT_TRANSACTIONAL";
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
        return context.services().stream()
                .flatMap(service -> service.compilationUnit().findAll(MethodDeclaration.class).stream()
                        .filter(this::looksLikeWriteMethod)
                        .filter(method -> !hasTransactional(method) && !service.annotations().contains("Transactional"))
                        .map(method -> issue(service, method)))
                .toList();
    }

    private boolean looksLikeWriteMethod(MethodDeclaration method) {
        String methodName = method.getNameAsString().toLowerCase(Locale.ROOT);
        return WRITE_PREFIXES.stream().anyMatch(methodName::startsWith);
    }

    private boolean hasTransactional(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getName().getIdentifier().equals("Transactional"));
    }

    private DetectedIssue issue(SourceFileContext service, MethodDeclaration method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Write service method is missing @Transactional",
                service.className() + "." + method.getNameAsString() + " looks like a write operation without @Transactional.",
                "Add @Transactional to service-layer write operations.",
                service.relativePath(),
                method.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
