package com.aperture.scan.service;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.engine.ProjectScanContext;
import com.aperture.scan.payload.RuleMetadataResponse;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleCatalogServiceTest {

    @Test
    void exposesRuleMetadataSortedByCategoryAndCode() {
        RuleCatalogService service = new RuleCatalogService(List.of(
                new TestRule("SECURITY_HARDCODED_SECRET", IssueCategory.SECURITY, IssueSeverity.CRITICAL),
                new TestRule("ARCH_CONTROLLER_REPOSITORY_DEPENDENCY", IssueCategory.ARCHITECTURE, IssueSeverity.HIGH)
        ));

        List<RuleMetadataResponse> rules = service.getRules();

        assertThat(rules)
                .extracting(RuleMetadataResponse::code)
                .containsExactly("ARCH_CONTROLLER_REPOSITORY_DEPENDENCY", "SECURITY_HARDCODED_SECRET");
        assertThat(rules.getFirst().title()).isEqualTo("Controller directly depends on Repository");
    }

    private record TestRule(String code, IssueCategory category, IssueSeverity defaultSeverity) implements ScannerRule {

        @Override
        public List<DetectedIssue> analyze(ProjectScanContext context) {
            return List.of();
        }
    }
}
