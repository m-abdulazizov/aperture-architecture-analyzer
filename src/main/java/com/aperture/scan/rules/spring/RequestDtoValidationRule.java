package com.aperture.scan.rules.spring;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.engine.SourceFieldContext;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class RequestDtoValidationRule implements ScannerRule {

    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
            "NotBlank",
            "NotEmpty",
            "NotNull",
            "Size",
            "Email",
            "Min",
            "Max",
            "Pattern",
            "Positive",
            "PositiveOrZero",
            "Negative",
            "NegativeOrZero"
    );

    @Override
    public String code() {
        return "SPRING_REQUEST_DTO_FIELD_WITHOUT_VALIDATION";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.SPRING;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.LOW;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        return context.sourceFiles().stream()
                .filter(sourceFile -> sourceFile.className().endsWith("Request"))
                .flatMap(requestDto -> requestDto.fields().stream()
                        .filter(field -> field.annotations().stream().noneMatch(VALIDATION_ANNOTATIONS::contains))
                        .map(field -> issue(requestDto, field)))
                .toList();
    }

    private DetectedIssue issue(SourceFileContext requestDto, SourceFieldContext field) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Request DTO field is missing validation",
                requestDto.className() + "." + field.name() + " has no validation annotation.",
                "Add validation annotations that describe accepted input.",
                requestDto.relativePath(),
                field.lineNumber()
        );
    }
}
