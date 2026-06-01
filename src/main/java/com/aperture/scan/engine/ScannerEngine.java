package com.aperture.scan.engine;

import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import com.aperture.scan.service.RuleConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScannerEngine {

    private final SourceFileDiscovery sourceFileDiscovery;
    private final ConfigFileDiscovery configFileDiscovery;
    private final JavaSourceParser javaSourceParser;
    private final List<ScannerRule> scannerRules;
    private final RuleConfigurationService ruleConfigurationService;

    public List<DetectedIssue> scan(UUID projectId, Path extractedProjectPath) {
        List<Path> javaFiles = sourceFileDiscovery.findJavaFiles(extractedProjectPath);
        List<Path> configFiles = configFileDiscovery.findConfigFiles(extractedProjectPath);
        List<SourceFileContext> sourceFiles = javaSourceParser.parseAll(extractedProjectPath, javaFiles);

        ProjectScanContext context = new ProjectScanContext(
                projectId,
                extractedProjectPath,
                javaFiles,
                configFiles,
                sourceFiles
        );

        return scannerRules.stream()
                .filter(scannerRule -> ruleConfigurationService.isEnabled(projectId, scannerRule.code()))
                .flatMap(scannerRule -> scannerRule.analyze(context).stream())
                .map(issue -> applySeverityOverride(projectId, issue))
                .toList();
    }

    private DetectedIssue applySeverityOverride(UUID projectId, DetectedIssue issue) {
        IssueSeverity severity = ruleConfigurationService.severityFor(projectId, issue.ruleCode(), issue.severity());
        if (severity == issue.severity()) {
            return issue;
        }

        return new DetectedIssue(
                issue.category(),
                severity,
                issue.ruleCode(),
                issue.title(),
                issue.description(),
                issue.recommendation(),
                issue.filePath(),
                issue.lineNumber()
        );
    }
}
