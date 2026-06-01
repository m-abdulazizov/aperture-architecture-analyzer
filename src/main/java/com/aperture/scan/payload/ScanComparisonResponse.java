package com.aperture.scan.payload;

import java.util.List;
import java.util.UUID;

public record ScanComparisonResponse(
        UUID fromScanResultId,
        UUID toScanResultId,
        int fromTotalScore,
        int toTotalScore,
        int scoreDelta,
        int newIssueCount,
        int fixedIssueCount,
        int persistentIssueCount,
        List<ScanIssueResponse> newIssues,
        List<ScanIssueResponse> fixedIssues,
        List<ScanIssueResponse> persistentIssues
) {
}
