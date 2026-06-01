package com.aperture.scan.payload;

public record RuleDocumentationResponse(
        String ruleCode,
        String markdown
) {
}
