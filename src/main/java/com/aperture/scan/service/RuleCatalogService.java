package com.aperture.scan.service;

import com.aperture.scan.payload.RuleMetadataResponse;
import com.aperture.scan.rules.ScannerRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleCatalogService {

    private final List<ScannerRule> scannerRules;

    public List<RuleMetadataResponse> getRules() {
        return scannerRules.stream()
                .map(this::toMetadata)
                .sorted(Comparator.comparing(RuleMetadataResponse::category)
                        .thenComparing(RuleMetadataResponse::code))
                .toList();
    }

    private RuleMetadataResponse toMetadata(ScannerRule rule) {
        RuleDescription description = describe(rule.code());
        return new RuleMetadataResponse(
                rule.code(),
                rule.category(),
                rule.defaultSeverity(),
                description.title(),
                description.description(),
                description.recommendation()
        );
    }

    private RuleDescription describe(String ruleCode) {
        return switch (ruleCode) {
            case "ARCH_CONTROLLER_REPOSITORY_DEPENDENCY" -> new RuleDescription(
                    "Controller directly depends on Repository",
                    "Detects controllers that inject or import repository types.",
                    "Route persistence access through a service layer."
            );
            case "ARCH_REPOSITORY_USED_OUTSIDE_SERVICE" -> new RuleDescription(
                    "Repository used outside service layer",
                    "Detects repository dependencies in non-service classes.",
                    "Keep database access inside service classes."
            );
            case "ARCH_ENTITY_RETURNED_FROM_CONTROLLER" -> new RuleDescription(
                    "Entity returned from controller",
                    "Detects controller methods returning JPA entity types.",
                    "Return DTOs from API endpoints."
            );
            case "SECURITY_HARDCODED_SECRET" -> new RuleDescription(
                    "Possible hardcoded secret",
                    "Detects likely credentials in application config files.",
                    "Move secrets to environment variables or a secret manager."
            );
            default -> new RuleDescription(
                    humanize(ruleCode),
                    "Detects " + humanize(ruleCode).toLowerCase() + ".",
                    "Review the finding and apply the recommended architectural practice."
            );
        };
    }

    private String humanize(String ruleCode) {
        String lower = ruleCode.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private record RuleDescription(String title, String description, String recommendation) {
    }
}
