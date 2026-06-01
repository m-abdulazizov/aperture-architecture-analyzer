package com.aperture.scan.scoring;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ScoreCalculator {

    private static final int STARTING_SCORE = 100;

    public ScoreBreakdown calculate(List<DetectedIssue> issues) {
        Map<IssueCategory, Integer> scores = new EnumMap<>(IssueCategory.class);
        for (IssueCategory category : IssueCategory.values()) {
            scores.put(category, STARTING_SCORE);
        }

        for (DetectedIssue issue : issues) {
            IssueCategory scoreCategory = scoreCategory(issue.category());
            scores.compute(scoreCategory, (category, currentScore) -> Math.max(0, currentScore - penalty(issue.severity())));
        }

        int architectureScore = scores.get(IssueCategory.ARCHITECTURE);
        int securityScore = scores.get(IssueCategory.SECURITY);
        int persistenceScore = scores.get(IssueCategory.PERSISTENCE);
        int maintainabilityScore = scores.get(IssueCategory.MAINTAINABILITY);
        int testingScore = scores.get(IssueCategory.TESTING);
        int totalScore = (architectureScore + securityScore + persistenceScore + maintainabilityScore + testingScore) / 5;

        return new ScoreBreakdown(
                totalScore,
                architectureScore,
                securityScore,
                persistenceScore,
                maintainabilityScore,
                testingScore,
                issues.size(),
                countBySeverity(issues, IssueSeverity.CRITICAL),
                countBySeverity(issues, IssueSeverity.HIGH),
                countBySeverity(issues, IssueSeverity.MEDIUM),
                countBySeverity(issues, IssueSeverity.LOW),
                countBySeverity(issues, IssueSeverity.INFO)
        );
    }

    private IssueCategory scoreCategory(IssueCategory category) {
        if (category == IssueCategory.SPRING) {
            return IssueCategory.MAINTAINABILITY;
        }
        return category;
    }

    private int penalty(IssueSeverity severity) {
        return switch (severity) {
            case CRITICAL -> 25;
            case HIGH -> 15;
            case MEDIUM -> 8;
            case LOW -> 3;
            case INFO -> 0;
        };
    }

    private int countBySeverity(List<DetectedIssue> issues, IssueSeverity severity) {
        return (int) issues.stream()
                .filter(issue -> issue.severity() == severity)
                .count();
    }
}
