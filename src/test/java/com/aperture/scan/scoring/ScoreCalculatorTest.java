package com.aperture.scan.scoring;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    @Test
    void calculatesScoresAndIssueCounts() {
        ScoreBreakdown score = new ScoreCalculator().calculate(List.of(
                issue(IssueCategory.SECURITY, IssueSeverity.CRITICAL),
                issue(IssueCategory.ARCHITECTURE, IssueSeverity.HIGH),
                issue(IssueCategory.SPRING, IssueSeverity.MEDIUM),
                issue(IssueCategory.PERSISTENCE, IssueSeverity.LOW),
                issue(IssueCategory.TESTING, IssueSeverity.INFO)
        ));

        assertThat(score.securityScore()).isEqualTo(75);
        assertThat(score.architectureScore()).isEqualTo(85);
        assertThat(score.maintainabilityScore()).isEqualTo(92);
        assertThat(score.persistenceScore()).isEqualTo(97);
        assertThat(score.testingScore()).isEqualTo(100);
        assertThat(score.totalScore()).isEqualTo(89);
        assertThat(score.totalIssues()).isEqualTo(5);
        assertThat(score.criticalIssues()).isEqualTo(1);
        assertThat(score.highIssues()).isEqualTo(1);
        assertThat(score.mediumIssues()).isEqualTo(1);
        assertThat(score.lowIssues()).isEqualTo(1);
        assertThat(score.infoIssues()).isEqualTo(1);
    }

    private DetectedIssue issue(IssueCategory category, IssueSeverity severity) {
        return new DetectedIssue(
                category,
                severity,
                "TEST_RULE",
                "Test issue",
                "Description",
                "Recommendation",
                "src/Test.java",
                1
        );
    }
}
