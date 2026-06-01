package com.aperture.scan.payload;

import com.aperture.scan.entity.IssueSeverity;

import java.util.UUID;

public record RuleConfigurationResponse(
        UUID id,
        UUID projectId,
        String ruleCode,
        boolean enabled,
        IssueSeverity severityOverride
) {
}
