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

@Component
public class GetListWithoutPaginationRule implements ScannerRule {

    @Override
    public String code() {
        return "SPRING_GET_LIST_WITHOUT_PAGINATION";
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
                .flatMap(controller -> controller.compilationUnit().findAll(MethodDeclaration.class).stream()
                        .filter(this::isGetMapping)
                        .filter(this::returnsCollection)
                        .filter(method -> !acceptsPagination(method))
                        .map(method -> issue(controller, method)))
                .toList();
    }

    private boolean isGetMapping(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getName().getIdentifier().equals("GetMapping"));
    }

    private boolean returnsCollection(MethodDeclaration method) {
        String returnType = method.getTypeAsString();
        return returnType.startsWith("List<") || returnType.startsWith("Collection<") || returnType.endsWith("[]");
    }

    private boolean acceptsPagination(MethodDeclaration method) {
        return method.getParameters().stream()
                .map(parameter -> parameter.getTypeAsString())
                .anyMatch(type -> type.equals("Pageable") || type.equals("PageRequest"));
    }

    private DetectedIssue issue(SourceFileContext controller, MethodDeclaration method) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "List endpoint does not accept pagination",
                controller.className() + "." + method.getNameAsString() + " returns a collection without pagination.",
                "Return Page<T> or accept Pageable for list endpoints.",
                controller.relativePath(),
                method.getRange().map(range -> range.begin.line).orElse(null)
        );
    }
}
