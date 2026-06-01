package com.aperture.scan.rules.security;

import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HardcodedSecretRule implements ScannerRule {

    private static final List<String> SECRET_KEYWORDS = List.of(
            "password",
            "secret",
            "token",
            "apikey",
            "api-key",
            "private-key",
            "access-key"
    );

    @Override
    public String code() {
        return "SECURITY_HARDCODED_SECRET";
    }

    @Override
    public IssueCategory category() {
        return IssueCategory.SECURITY;
    }

    @Override
    public IssueSeverity defaultSeverity() {
        return IssueSeverity.CRITICAL;
    }

    @Override
    public List<DetectedIssue> analyze(ProjectScanContext context) {
        List<DetectedIssue> issues = new ArrayList<>();

        for (Path configFile : context.configFiles()) {
            try {
                List<String> lines = Files.readAllLines(configFile);
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    if (containsSecretKeyword(line) && containsConcreteValue(line)) {
                        issues.add(issue(context, configFile, index + 1));
                    }
                }
            } catch (IOException ignored) {
                // The scanner should keep running if a single config file cannot be read.
            }
        }

        return issues;
    }

    private boolean containsSecretKeyword(String line) {
        String lowerLine = line.toLowerCase(Locale.ROOT);
        return SECRET_KEYWORDS.stream().anyMatch(lowerLine::contains);
    }

    private boolean containsConcreteValue(String line) {
        String trimmed = line.trim();

        if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
            return false;
        }

        int separator = Math.max(trimmed.indexOf('='), trimmed.indexOf(':'));
        if (separator < 0 || separator == trimmed.length() - 1) {
            return false;
        }

        String value = trimmed.substring(separator + 1).trim();
        return !value.isBlank()
                && !value.startsWith("${")
                && !value.equals("\"\"")
                && !value.equals("''");
    }

    private DetectedIssue issue(ProjectScanContext context, Path configFile, int lineNumber) {
        return new DetectedIssue(
                category(),
                defaultSeverity(),
                code(),
                "Possible hardcoded secret",
                "Configuration file contains a possible hardcoded secret value.",
                "Move secrets to environment variables or a secret manager and reference placeholders in configuration.",
                context.rootDirectory().relativize(configFile).toString().replace('\\', '/'),
                lineNumber
        );
    }
}
