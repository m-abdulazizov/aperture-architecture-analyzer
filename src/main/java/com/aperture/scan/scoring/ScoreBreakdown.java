package com.aperture.scan.scoring;

public record ScoreBreakdown(
        int totalScore,
        int architectureScore,
        int securityScore,
        int persistenceScore,
        int maintainabilityScore,
        int testingScore,
        int totalIssues,
        int criticalIssues,
        int highIssues,
        int mediumIssues,
        int lowIssues,
        int infoIssues
) {
}
