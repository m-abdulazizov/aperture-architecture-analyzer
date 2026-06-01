package com.aperture.scan.payload;

import java.util.List;
import java.util.UUID;

public record QualityGateResponse(
        UUID scanResultId,
        boolean passed,
        int minimumScore,
        int actualScore,
        int maxCriticalIssues,
        int actualCriticalIssues,
        int maxHighIssues,
        int actualHighIssues,
        List<String> failures
) {
}
