package com.aperture.scan.engine;

import com.aperture.scan.config.RuleProperties;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
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
    private final RuleProperties ruleProperties;

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
                .filter(scannerRule -> !ruleProperties.getDisabled().contains(scannerRule.code()))
                .flatMap(scannerRule -> scannerRule.analyze(context).stream())
                .map(this::applySeverityOverride)
                .toList();
    }

    private DetectedIssue applySeverityOverride(DetectedIssue issue) {
        IssueSeverity override = ruleProperties.getSeverityOverrides().get(issue.ruleCode());
        if (override == null) {
            return issue;
        }

        return new DetectedIssue(
                issue.category(),
                override,
                issue.ruleCode(),
                issue.title(),
                issue.description(),
                issue.recommendation(),
                issue.filePath(),
                issue.lineNumber()
        );
    }
}
