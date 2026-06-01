package com.aperture.scan.payload;

import com.aperture.scan.entity.IssueSeverity;

public record RuleConfigurationRequest(
        Boolean enabled,
        IssueSeverity severityOverride
) {
}
