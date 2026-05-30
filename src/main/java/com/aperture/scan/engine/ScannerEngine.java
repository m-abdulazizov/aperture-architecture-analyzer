package com.aperture.scan.engine;

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
                .flatMap(scannerRule -> scannerRule.analyze(context).stream())
                .toList();
    }
}
